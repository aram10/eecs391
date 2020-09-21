2/6/2020 Programming Assignment 1

Alexander Rambasek

ResourceCollectionAgent
-I made modifications to the ResourceCollectionAgent so that Peasants now build farms when they are able to.
-Peasants can build Barracks, and Footman can spawn in the first Barrack (up to 3)
-At least one Barracks must exist before Farms are made, for footman production to start
-Once enough resources are obtained for a structure, one peasant will be assigned to construct it where he is currently standing
-The first time around, I tried to have peasants only build in a certain area, or in an area closest to the townhall. However, the problem
I ran into was that peasants would routinely surround themselves with buildings and become trapped. Thus, I switched from COMPOUNDBUILD back
to PRIMITIVEBUILD to just have the peasants build in place when sufficient resources are obtained.
-Having farms and barracks built with equal weight was also problematic: peasants would only build farms, sucking up all the resources from peasants 
assigned to build barracks. So, I mandated that at least one Barracks exists for Footmen to begin spawning before Farms are built.

CombatAgent
-Adapted strategy so that agents begin the fight by attacking the leftmost attackers (furthest from the tower because that has been wrecking my army).
-After the initialStep, the agents are split up evenly betweeen attacking closest enemy, and the enemy with the lowest health
-My first attempt with this involved me trying to designate one footman as "bait", who would inch closer to the enemy army in an attempt to draw the units into an ambush. However,
I scrapped that since it never seemed to be enough of an advantage, and also I couldn't figure out how to get the other units to wait for the death of the bait to begin their assault.
I tried to do it with the DeathLog, but that required that I specify a turn in which the death occurred, which I couldn't say for sure.
-My army still cannot beat the enemy army, but I have drawn out battles a little longer, gotten a higher kill count than before, and caused the enemy soldiers to be in a location such that if I 
were to have ballistae/archer towers behind my army, they would likely be finished off.

Experience with the documentation: If I'm being honest, navigating throught the documentation was tricky. I spent the first few hours of this project trying to figure out what extends what,
and digging to find certain classes that implement the functionality that I desired. I wish that methods and classes had more context given as to their intended use, instead of simply what 
they take and return.

I found the extra layer of abstraction in the "Templates" and "Views" to be confusing; I was always struggling to figure out when I would use one particular class or it's template/view variant 
that extends it. I feel like the class heirarchy could be a little less convoluted.


