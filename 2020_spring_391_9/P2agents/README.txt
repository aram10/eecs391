Date of commit: 2/10/2020
@author Alexander Rambasek
I wrote helper methods and began to design the overall skeleton of the A* search algorithm. Comments explaining what everything does and next steps.
------------------------------------------------------------------------------------------------------------------------------------------------------------

Date of commit: 2/12/2020
@author Stamatis Papadopoulos
Fixed logic errors in some helper methods. Made comments easier to read. Began A* search algorithm. Ran into issues with calculating the g(n) for each node. to be continued
------------------------------------------------------------------------------------------------------------------------------------------------------------
Date of commit: 2/12/2020
@author Stamatis Papadopoulos
Found that our MapLocation was missing a parent and a cost part. changed lowestCost() calculation to include the cost to the node, as well as getValidNeighbors() and added onto Astar to create a better skeleton.
------------------------------------------------------------------------------------------------------------------------------------------------------------
Date of commit: 2/13/2020
@author Stamatis Papadopoulos
Filled out Astar to add neighbors to the open list. Checks the open and closed lists first before deciding to add. Now need a reconstructPath() helper function.
------------------------------------------------------------------------------------------------------------------------------------------------------------

Date of commit: 2/15/2020
@author Alexander Rambasek
Created a helper method reconstructPath, which takes a node that is adjacent to the goal and adds all its predecessors in order to the path stack.
------------------------------------------------------------------------------------------------------------------------------------------------------------

Date of commit: 2/16/2020
@author Alexander Rambasek
Debugged console errors, commented code, got it to output a path, but it did not consider a tile with a tree. Pushing for Stamatis to have a look.
------------------------------------------------------------------------------------------------------------------------------------------------------------

Date of commit: 2/17/2020
@author Alexander Rambasek
Fixed algorithm so that it correctly identifies the shortest path, implemented shouldReplanPath().
------------------------------------------------------------------------------------------------------------------------------------------------------------


Alex's thoughts: I did not really interact with the documentation much in this assignment, as a lot of the SEPIA specific code was already written, and I could figure out appropriate classes
and methods by inspecting the rest of the document. I attempted to utilize the History class to determine assigned actions in an attempt to give the agent an edge in seeing what primitive moves
the enemy footman has, but this was ultimately unsuccessful. I feel like there is a good deal of complexity in the organization of the SEPIA classes, without sufficient documentation detailing 
their uses. I was lost in an endless cycle of expanding hyperlinks to pages to then expand more hyperlinks, in an attempt to get a good idea of what a method actually does.
