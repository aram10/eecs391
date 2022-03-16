@author Alexander Rambasek

A few things that I'd like to make known:

-One issue that my code was having is that for some reason, the agent has 200 wood less than it thinks it does. I've shared this problem with Madhu and we have not been able to determine what is causing it. I suspect that, somehow, when a parallel harvest action is being evaluated more wood is being added to the STRIPS representation then in SEPIA. However, it has been consistently 200 wood, and not gold. I've struggled to find the cause of this problem but to no avail. So, I eventually decided to artificially add 200 wood to the isGoal() check to offset this.

-It seems to me that the large map is 200 wood short of having the required 2000 wood for peasants to collect. However, I don't think this issue is related to the previous issue, oddly enough. This is because when I set the scenario to have a wood requirement of 2000, even with the artificial offset in GameState, a plan is not able to be found. Furthermore, I checked the inventories of the peasants at the end of the scenario and they were not carrying anything. So, only 1800 wood collected out of 2000, with no more tree icons left on the map and peasants with empty inventories. Even thought I can't find a link from this to the previous issue, it would certainly be a freaky coincidence if they were not related somehow (what's so special about 200 wood). This makes me think that the issue may be something with recognizing and encapsulating resources into STRIPS.

-The execution time was calculated by summing the total amount of time my program spent in iterations of the middleStep() method in PEagent.

-I overrided the toString() method of every class that implements StripsAction, so the plan is printed out in English instead of just the memory locations of the action objects.


(a) 
required wood: 1000 required gold: 1000 build Peasants: true 
Config: midasSmall_BuildPeasant.xml
Total execution time: 0.0088075 seconds.

Peasant with ID 1 moves to position with coordinates (4, 7)
Peasant with ID 1 harvests from resource with position (4, 7)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (17, 9)
A new peasant is built.
Peasant with ID 1 harvests from resource with position (17, 9)
Peasant with ID 10 moves to position with coordinates (15, 3)
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasants with ids {10, 1} harvest from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (4, 4)
A new peasant is built.
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (4, 4)
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasants with ids {11, 10} harvest from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 1 moves to position with coordinates (4, 7)
Peasant with ID 10 moves to position with coordinates (15, 3)
Peasants with ids {1, 10} move to coordinates (12, 4).
Peasants with ids {10, 1} harvest from resource with position (12, 4)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 moves to position with coordinates (4, 7)
Peasant with ID 11 moves to position with coordinates (12, 4)
Peasants with ids {11, 10} harvest from resource with position (12, 4)
Peasant with ID 1 moves to position with coordinates (4, 4)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 moves to position with coordinates (4, 4)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (4, 4)
Peasants with ids {1, 11, 10} harvest from resource with position (4, 4)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (4, 12)
Peasant with ID 10 harvests from resource with position (4, 12)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (12, 13)
Peasant with ID 10 harvests from resource with position (12, 13)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (4, 12)
Peasant with ID 10 harvests from resource with position (4, 12)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (12, 13)
Peasant with ID 10 harvests from resource with position (12, 13)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (4, 12)
Peasant with ID 10 harvests from resource with position (4, 12)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing wood at town hall.
Peasant with ID 10 moves to position with coordinates (4, 12)
Peasant with ID 10 harvests from resource with position (4, 12)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasants with ids {1, 10, 11} depositing at the town hall.



(b) 
required wood: 1800 required gold: 3000 build Peasants: true
Config: midasLarge_BuildPeasant.xml
Total execution time: 0.0159803 seconds.

Peasant with ID 1 moves to position with coordinates (4, 7)
Peasant with ID 1 harvests from resource with position (4, 7)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasant with ID 1 harvests from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (17, 9)
A new peasant is built.
Peasant with ID 1 harvests from resource with position (17, 9)
Peasant with ID 10 moves to position with coordinates (15, 3)
Peasant with ID 1 moves to position with coordinates (15, 3)
Peasants with ids {10, 1} harvest from resource with position (15, 3)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (4, 4)
A new peasant is built.
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (4, 4)
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasants with ids {11, 10} harvest from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasant with ID 1 harvests from resource with position (22, 16)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (22, 16)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {11, 1} harvest from resource with position (22, 16)
Peasant with ID 10 depositing gold at town hall.
Peasant with ID 10 moves to position with coordinates (22, 16)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 harvests from resource with position (22, 16)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 1 depositing gold at town hall.
Peasant with ID 1 moves to position with coordinates (22, 16)
Peasants with ids {10, 1} harvest from resource with position (22, 16)
Peasant with ID 1 moves to position with coordinates (4, 4)
Peasant with ID 10 moves to position with coordinates (4, 4)
Peasants with ids {1, 10} harvest from resource with position (4, 4)
Peasant with ID 11 depositing gold at town hall.
Peasant with ID 11 moves to position with coordinates (4, 12)
Peasant with ID 1 moves to position with coordinates (8, 9)
Peasant with ID 10 moves to position with coordinates (8, 9)
Peasant with ID 11 harvests from resource with position (4, 12)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (4, 12)
Peasant with ID 11 harvests from resource with position (4, 12)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (4, 12)
Peasant with ID 11 harvests from resource with position (4, 12)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (4, 12)
Peasant with ID 11 harvests from resource with position (4, 12)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 harvests from resource with position (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 harvests from resource with position (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 harvests from resource with position (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 harvests from resource with position (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (17, 9)
Peasant with ID 11 harvests from resource with position (17, 9)
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (17, 9)
Peasant with ID 11 harvests from resource with position (17, 9)
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (17, 9)
Peasant with ID 11 harvests from resource with position (17, 9)
Peasant with ID 11 moves to position with coordinates (12, 13)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (4, 4)
Peasant with ID 11 harvests from resource with position (4, 4)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 4)
Peasant with ID 11 harvests from resource with position (12, 4)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 4)
Peasant with ID 11 harvests from resource with position (12, 4)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 4)
Peasant with ID 11 harvests from resource with position (12, 4)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasant with ID 11 depositing wood at town hall.
Peasant with ID 11 moves to position with coordinates (12, 4)
Peasant with ID 11 harvests from resource with position (12, 4)
Peasant with ID 11 moves to position with coordinates (8, 9)
Peasants with ids {1, 10, 11} depositing at the town hall.
