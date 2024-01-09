package org.example;

import aima.core.environment.wumpusworld.*;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;


public class EnvironmentAgent extends Agent {
    public static final String ServiceName = "WumpusKrampus";
    public static final String ServiceType = "Environment";

    private final WumpusEnvironment wumpusEnvironment = new WumpusEnvironment(new WumpusCave(4, 4, ""
            + ". . . P "
            + "W G P . "
            + ". . . . "
            + "S . P . "));

    private final EfficientHybridWumpusAgent aimaWumpusAgent = new EfficientHybridWumpusAgent(4, 4, new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH));

    protected void setup() {
        System.out.println(getAID());

        // register service
        var dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        var serviceDescription = new ServiceDescription();
        serviceDescription.setType(ServiceType);
        serviceDescription.setName(ServiceName);
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        // register logic
        wumpusEnvironment.addAgent(aimaWumpusAgent);

        // receive requests
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg == null) {
                    block();
                    return;
                }
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    AgentPosition agentPosition = wumpusEnvironment.getAgentPosition(aimaWumpusAgent);
                    System.out.println("Environment state request. Speleologist's position " + agentPosition);

                    WumpusPercept wumpusPercept = wumpusEnvironment.getPerceptSeenBy(aimaWumpusAgent);

                    ACLMessage reply = msg.createReply(ACLMessage.INFORM);
                    reply.setContent(wumpusPercept.toString());
                    myAgent.send(reply);

                    System.out.println("Replied with the Perception: " + wumpusPercept);
                }
                else if (msg.getPerformative() == ACLMessage.INFORM) {
                    String move = msg.getContent();
                    WumpusAction action = WumpusAction.valueOf(move);
                    wumpusEnvironment.execute(aimaWumpusAgent, action);
                    System.out.println("Environment acknowledged of Speleologist made move: " + action);
                    var reply = msg.createReply(ACLMessage.CONFIRM);
                    myAgent.send(reply);
                }
            }
        });
    }


    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " takeDown");
    }
}