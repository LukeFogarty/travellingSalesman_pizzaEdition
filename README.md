# travellingSalesman_pizzaEdition
A solution to a college project about solving the Travelling Salesman Problem

# The Project
The aim of the project was to find the quickest route between input houses to minimise the time spent waiting over thirty minutes by customers. Any minute over 30 was counted as an 'angry minute', so our result was based on the amount of angry minutes we had after running through the route.

We were given some sample data for testing, which had to be input in a specific order: delivery number, address, minutes waiting, GPS North and GPS West. Distance between each address needed to be calculated, and minutes waiting also had to be calculated.

When ran, the application had to output a list of the houses visited in order.

 # My Solution
I used a Haversine formula to find the distance between the houses, and stored that information in a distance matrix array. This distance could be easily checked using the deliviery number of the houses you wanted to check. After that, I used two lists and a Nearest Neighbour algorithm (that slightly favours houses with higher waiting time) to find a good route between them. Then I randomised that 'optimal route' three million times to see if I could find a shorter distance than the one the Nearest Neighbour generated. Finally, I ran the route through a 2-opt algorithm to find any possible cross-overs to try and shorten the distance again.

# The Graphics
Aside from the solution, we were graded on the graphical output of the project. I tried to make it as clear and straight-forward as possible. The large text box is for inputting information, and the small text box returns the route in order of delivery number and also outputs any errors like missing or incorrect data. Once the route is calculated, houses are placed on the land, and a small motorbike driver zips from address to address followed by a dotted red line, until they reach the final house. An angry looking clock keeps track of the total 'Angry Minutes' so far.

# Use
You can download and use this freely. There is sample data provided, and if you want you can make your own data to test it out!
