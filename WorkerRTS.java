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
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

/**
   This example shows how to implement the responder role in 
   a FIPA-request interaction protocol. In this case in particular 
   we use an <code>AchieveREResponder</code> ("Achieve Rational effect") 
   to serve requests to perform actions from other agents. We use a 
   random generator to simulate request refusals and action execution 
   failures.
   @author Giovanni Caire - TILAB
 */
public class WorkerRTS extends Agent {

	public static int timeHarvest = 3;
	public static int timeProduce = 5;

	private AID managerAID;

	private class WorkBehaviour extends Behaviour {
		/*
		 * The assigned task of the Worker :
		 * 	0 -> nothing
		 * 	1 -> harvesting
		 * 	2 -> producing (using resources to produce crafts)
		 * 	3 -> stopping
		 */
		private int current_task = 0;
		private int time_left = 0;

		public void action() {
			if (current_task == 1 || current_task == 2)
			{
				time_left --;
				if (time_left <= 0)
				{
					((WorkerRTS) myAgent).notifyTaskDone(current_task);
					current_task = 0;
				}
			}
			ACLMessage msg = myAgent.receive();
			if (msg != null)
			{
				// empty message queue
				ACLMessage latest_msg = myAgent.receive();
				while (latest_msg != null) {
					msg = latest_msg;
					latest_msg = myAgent.receive();
				}
				// msg is the latest message sent by the Manager
				switch (msg.getContent())
				{
					case ManagerRTS.PAUSE:
						current_task = 0;
						break;
					case ManagerRTS.HARVEST:
						if (current_task != 1)
						{
							current_task = 1;
							time_left = timeHarvest;
						}
						break;
					case ManagerRTS.PRODUCE:
						if (current_task != 2)
						{
							current_task = 2;
							time_left = timeProduce;
						}
						break;
					case ManagerRTS.FINISH:
						current_task = 3;
						break;
				}
			}
		}
		
		public final boolean done() {
			return (current_task == 3);
		}
	}

	protected void setup() {
		System.out.println("Agent "+getLocalName()+" waiting for Manager...");

		ACLMessage managerGreet = blockingReceive();
		managerAID = managerGreet.getSender();
		System.out.println("Agent "+getLocalName()+" received "+managerGreet.getContent());

		System.out.println("Agent "+getLocalName()+" ready to do stuff");

		addBehaviour(new WorkBehaviour());
	}

	public void notifyTaskDone(int task) {
		ACLMessage taskDone = new ACLMessage(ACLMessage.INFORM);
		taskDone.addReceiver(managerAID);
		switch (task) {
			case 1 :
				taskDone.setContent(ManagerRTS.HARVEST);
				break;
			case 2 :
				taskDone.setContent(ManagerRTS.PRODUCE);
				break;
		}
		send(taskDone);
	}
}

