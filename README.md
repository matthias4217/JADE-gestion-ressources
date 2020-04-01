# JADE Resource Manager

A small school project modeling resource management.

![Agents sending messages](https://github.com/matthias4217/JADE-gestion-ressources/blob/master/jade_icon.png)

## Objectives

Using [Jade](https://jade.tilab.com/), create a simulation of a manager organising some workers to collect resources and produce items.
Workers have to be able to harvest a resource, and produce using resources.
The Manager has to know the amount of available resources and products, and to ask Workers either to produce or to harvest in order to fulfill a production objective.
Then, we would run a few simulations to compare different strategies with different objectives and a different number of Workers.

## Work achieved

We made the Worker on a state machine model : the Worker is either doing nothing (PAUSE), harvesting, producing, or has completely stopped.
The Manager first contacts each Worker, asking them to harvest, since at the begining, there is no resource at all.
Harvesting and producing are actions that take a pre-determined time. When a worker is done, it notifies the Manager, which then replies with a new order, determined depending on the strategy.
The Manager keeps track of the amount of resources and products.
The simulation keeps going on until the manager has reached the production objective. Then, it sends a signal to all workers to stop.

We implemented two strategies :
- "Produce if you can" consists in asking to produce whenever we have the available resource.
- "Harvesting ratio" consists in using probabilities to have approximately a percentage of the workers doing each task.

After some tests, the "Produce if you can" strategy proved being more efficient. It would be interresting to test more strategies in a slightly more complex setup, with more resources, more products, or with objectives both on products and resources.
