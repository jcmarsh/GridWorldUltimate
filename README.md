src/controllers: Has some example controllers. These in general are not tested and may even have dependencies within gridworld. Controllers should communicate with gridworld solely through a socket (see src/gridworld/SocketHandler.java).
src/gridworld: The meat of the code. Defines the environment, the robots in it, and the sensors that they use.
src/gridworld/environments: For right now, just a grid environment, which can be either continuous or discrete.
src/gridworld/sensors: A collection of sensors that the robot may have. RangeSensor is by far the most complicated.
src/simulator: An attempt to generalize the code base as much as possible. As the code is refactored, this is expected to grow into a simulation framework which can support a variety of environments, platforms, physics models, communication models, and interfaces.

GIANT TODO LIST OF DOOM AND/OR DESTINY:
Immediate:
* New environment to support boundary estimation problem
* New sensor to sense field for boundary estimation problem
* Controller for boundary estimation problem
Soon:
* Separate Discrete and Continuous cases for GridWorld into two separate environments
* Document the communication interface
OneDay: 
* Incorporate Actuators in a similar manner as Sensors
* Tie in a physics library and / or support collisions
* Automate building the communication interfaces
