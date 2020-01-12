/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package examples.ombre;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;

import java.util.Date;
import java.lang.System;
import java.util.Vector;

/**
 */
public class ManagerRTS extends Agent {
	
    public long startupTime = System.currentTimeMillis();
        /** @Unused Resources owned by the manager */	
        public int[] resources = {0, 0};


	public static int strategie=0; //0: produce when possible; 1: dynamic probability of harvest

	public static int productCost = 3;
	
	// the current resource stock
	private int nbResource = 0;
	// the current product stock
	private int nbProduct = 0;
	// the product goal
	private int goalProduct = 3;
	private boolean hasStarted = false;
	
	public static final String PAUSE = "Pause";	
	public static final String HARVEST = "Harvest";
	public static final String PRODUCE = "Produce";
	public static final String FINISH = "Finish";
	public static final String HELLO = "Hello";

	public int nWorkers=0;
	public Object[] args;
	

	private class ManagerBehaviour extends Behaviour {

		private int current_worker = 0;
		
		public void action() {
                    int nbWorkers = ((ManagerRTS) myAgent).nWorkers;
                    Object[] args =  ((ManagerRTS) myAgent).args;
                    if (nbWorkers > 0 && !hasStarted) {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setContent(HARVEST);
				for(current_worker = 0 ; current_worker < nbWorkers ; current_worker++)
				{
					msg.addReceiver(new AID((String) args[current_worker], AID.ISLOCALNAME));
				}
				myAgent.send(msg);
				hasStarted = true;
                    }
		    // Receive information from Workers
		    ACLMessage workerInfo = myAgent.receive();
		    while (workerInfo != null)
		    {
			    switch (workerInfo.getContent())
			    {
				    case HARVEST:
					    nbResource++;
					    break;
				    case PRODUCE:
					    nbProduct++;
					    break;
			    }
			    ACLMessage reply = workerInfo.createReply();
			    reply.setContent(determineOrder());
			    myAgent.send(reply);

			    workerInfo = myAgent.receive();
		    }
		}

		public final boolean done() {
                    return (nbProduct >= goalProduct);
		}

                public int onEnd() {
                    long executionTime = System.currentTimeMillis()
                        - ((ManagerRTS) myAgent).startupTime;
                    System.out.println("Total duration: "+executionTime);
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setContent(FINISH);
                    for(int current_worker=0;current_worker< nWorkers;current_worker++)
                    {
                        msg.addReceiver(new AID((String) args[current_worker], AID.ISLOCALNAME));
                    }
                    myAgent.send(msg);
                    return 1;
                }
	}

	private String determineOrder() {
		if(strategie==0)
		{
			return produceIfYouCan();
		}
		else
		{
			return harvestingRatio();
		}
	}
	
	private String produceIfYouCan() {
		// if has enough to produce, produce
		if (nbResource >= productCost)
		{
			nbResource -= productCost;
			return PRODUCE;
		}
		return HARVEST;
	}

	private String harvestingRatio() {
		float ratio =  ((float)nbProduct / ((float)goalProduct+1)  );
		if (Math.random() < ratio)
		{
			return HARVEST;
		}

		if (nbResource >= productCost)
		{
			nbResource -= productCost;
			return PRODUCE;
		}
		return HARVEST;
	}

	protected void setup() {

            args = getArguments();
            if (args != null) {	
                nWorkers = args.length;
            }

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent(HELLO);
            for(int current_worker=0;current_worker< nWorkers;current_worker++)
            {
                msg.addReceiver(new AID((String) args[current_worker], AID.ISLOCALNAME));
            }
            this.send(msg);


            System.out.println("Manager "+getLocalName()+" prepares to send requests...");
		/*MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );*/

            addBehaviour(new ManagerBehaviour());
	}
	
/*
  protected void setup() {
  	// Read names of responders as arguments
  	Object[] args = getArguments();
  	if (args != null && args.length > 0) {
  		nResponders = args.length;
  		System.out.println("Requesting dummy-action to "+nResponders+" responders.");
  		
  		// Fill the REQUEST message
  		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  		for (int i = 0; i < args.length; ++i) {
  			msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
  		}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			// We want to receive a reply in 10 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			msg.setContent("dummy-action");
			
			addBehaviour(new AchieveREInitiator(this, msg) {
				protected void handleInform(ACLMessage inform) {
					System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
				}
				protected void handleRefuse(ACLMessage refuse) {
					System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
					nResponders--;
				}
				protected void handleFailure(ACLMessage failure) {
					if (failure.getSender().equals(myAgent.getAMS())) {
						// FAILURE notification from the JADE runtime: the receiver
						// does not exist
						System.out.println("Responder does not exist");
					}
					else {
						System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
					}
				}
				protected void handleAllResultNotifications(Vector notifications) {
					if (notifications.size() < nResponders) {
						// Some responder didn't reply within the specified timeout
						System.out.println("Timeout expired: missing "+(nResponders - notifications.size())+" responses");
					}
				}
			} );
  	}
  	else {
  		System.out.println("No responder specified.");
  	}
  }*/ 
}

