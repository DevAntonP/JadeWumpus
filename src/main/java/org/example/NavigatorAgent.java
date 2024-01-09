package org.example;

import aima.core.environment.wumpusworld.*;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NavigatorAgent extends Agent {

    public static final String ServiceType = "Navigator";

    private final EfficientHybridWumpusAgent aimaWumpusAgent = new EfficientHybridWumpusAgent(4, 4, new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH));

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

        // Navigator job behaviour
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage call = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
                if (call != null) {
                    String perception = call.getContent();
                    WumpusPercept percept = WumpusLanguage.ParsePerceptMessage(perception);

                    WumpusAction action = aimaWumpusAgent.act(percept).orElseThrow();
                    ACLMessage moveProposition = call.createReply(ACLMessage.PROPOSE);
                    String moveMsg = WumpusLanguage.CreateActionMessage(action);
                    moveProposition.setLanguage(WumpusLanguage.Language);
                    moveProposition.setContent(moveMsg);
                    System.out.println("Navigator suggested move: " + moveMsg);
                    myAgent.send(moveProposition);
                } else {
                    block();
                }
            }
        });
    }

    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " takeDown");
    }
}