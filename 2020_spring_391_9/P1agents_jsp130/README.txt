Stamatis Papadopoulos

jsp130ResourceAgent:

Begins by building 3 peasants, the building a farm, then a barracks, then building a footman.
After it has completed that, it will alternate building a farm and then a barracks in a neat
line until it reaches the edge of the map, where it will move one row down and continue.

jsp130CombatAgent:
At first, I move each unit into position. Then I wanted units to attack the closest unit.
I also wanted them to retreat if damaged in the damage log, so any unit damaged
according to the damage log will move to the bottom left corner. For the combat, I resorted
to creating a simpler approach where units would attack the most damaged unit as long as 
one existed. The closest enemy would be targeted otherwise. I originally calculated the
closest enemy trying to use the distance formula but for some reason the units wouldn't 
respond so I resorted to just calculating the X coordinate because that works in most cases.


Experience:
The documentation itself is ok, although a bit difficult to navigate, especially given how 
many classes there are to look at. Some sort of search function for methods would have been
a nice inclusion. Other than that, I couldn't find a way to actually look at the class or method
beyond a simple name, parameters, and brief description. That would have helped to understand
the code better. I had a lot of trouble with the Combat Agent if I'm being entirely honest.
I spent hours trying to make the units do what I wanted and they usually didn't. I could not
figure out why.