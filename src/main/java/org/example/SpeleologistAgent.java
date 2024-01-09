package org.example;

import aima.core.environment.wumpusworld.WumpusAction;
import aima.core.environment.wumpusworld.WumpusPercept;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;


public class SpeleologistAgent extends Agent {

    public static final String ServiceType = "Speleologist";
    private AID environmentAid;
    private AID navigatorAid;

    protected void setup() {
        System.out.println(getAID());

        // register service
        var dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        var serviceDescription = new ServiceDescription();
        serviceDescription.setType(ServiceType);
        serviceDescription.setName(EnvironmentAgent.ServiceName);
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        // look for the Environment agent
        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                var agentDescription = new DFAgentDescription();
                var sd = new ServiceDescription();
                sd.setType(EnvironmentAgent.ServiceType);
                agentDescription.addServices(sd);

                DFAgentDescription[] result;
                try {
                    result = DFService.search(myAgent, agentDescription);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }

                if (result.length == 0) {
                    return;
                }

                environmentAid = result[0].getName();
                System.out.println(getAID().getLocalName() + " found the " + environmentAid.getLocalName());
                this.stop();
            }
        });

        // Look for the Navigator agent
        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                var agentDescription = new DFAgentDescription();
                var sd = new ServiceDescription();
                sd.setType(NavigatorAgent.ServiceType);
                agentDescription.addServices(sd);

                DFAgentDescription[] result;
                try {
                    result = DFService.search(myAgent, agentDescription);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }

                if (result.length == 0) {
                    return;
                }

                navigatorAid = result[0].getName();
                System.out.println(getAID().getLocalName() + " found the " + navigatorAid.getLocalName());
                this.stop();
            }
        });

        // All is set up. Begin the Speleologist job.
        addBehaviour(new TickerBehaviour(this, 2000) {
            protected void onTick() {
                if (environmentAid != null && navigatorAid != null) {
                    System.out.println(getAID().getLocalName() + " is ready and starting the journey.");
                    addBehaviour(new SpeleologistBehaviour());
                    this.stop();
                }
            }
        });
    }

    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " takeDown");
        try {
            getContainerController().getAgent(navigatorAid.getLocalName()).kill();
            getContainerController().getAgent(environmentAid.getLocalName()).kill();
        } catch (ControllerException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    class SpeleologistBehaviour extends Behaviour {
        WumpusAction wumpusAction;
        private WumpusPercept currentPercept;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0 -> {
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(environmentAid);
                    myAgent.send(request);
                    System.out.println("Speleologist requests the Environment for a perception");
                    step = 1;
                }
                case 1 -> {
                    ACLMessage reply = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                    if (reply != null) {
                        String percept = reply.getContent();
                        currentPercept = WumpusPerceptExtended.fromString(percept);
                        System.out.println("Speleologist received the perception: " + currentPercept);

                        step = 2;
                    } else {
                        block();
                    }
                }
                case 2 -> {
                    ACLMessage navigationRequest = new ACLMessage(ACLMessage.CFP);
                    navigationRequest.setLanguage(WumpusLanguage.Language);
                    navigationRequest.setOntology(WumpusLanguage.Ontology);
                    navigationRequest.addReceiver(navigatorAid);
                    var perceptMessage = WumpusLanguage.CreatePerceptMessage(currentPercept);
                    navigationRequest.setContent(perceptMessage);
                    myAgent.send(navigationRequest);
                    System.out.println("Speleologist asks the Navigator for help: " + perceptMessage);
                    step = 3;
                }
                case 3 -> {
                    ACLMessage reply = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    if (reply != null) {
                        String actionMessage = reply.getContent();
                        wumpusAction = WumpusLanguage.ParseActionMessage(actionMessage);
                        System.out.println("Speleologist received advice for an action: " + wumpusAction);
                        step = 4;
                    } else {
                        block();
                    }
                }
                case 4 -> {
                    ACLMessage action = new ACLMessage(ACLMessage.INFORM);
                    action.addReceiver(environmentAid);
                    action.setContent(wumpusAction.name());
                    System.out.println("Speleologist make move");
                    myAgent.send(action);
                    step = 5;
                }
                case 5 -> {
                    ACLMessage approveMsg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
                    if (approveMsg != null) {
                        if (wumpusAction.equals(WumpusAction.CLIMB)) {
                            System.out.println("Speleologist climbed out successfully");
                            step = 6;
                        } else {
                            System.out.println("Speleologist: \"Here we go again\"");
                            step = 0;
                        }
                    } else {
                        block();
                    }
                }
                case 6 -> {
                    System.out.println("Game over!");
                    step = 7;
                    myAgent.doDelete();
                }
            }
        }

        public boolean done() {
            return step == 7;
        }
    }
}
