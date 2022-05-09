# MAPF-CBS

An implementation of the Conflict-Based Search algorithm [1].

In the Multi-agent Pathfinding problem, we are given a set of _n_ robotic agents with start and target positions on a
graph.
The goal is to find a path for each agent, a sequence of moves along the graph's edges, such that:

1. The paths do not collide. For timestep _t_, and any pair of agents _a_ and _b_, the following conditions must be
   met:
    - Agents _a_ and _b_ cannot occupy the same vertex at timestep _t_. We call this a _vertex conflict_.
    - If agent _a_ moves between vertices _v1_ and _v2_ at timestep _t -> t+1_, agent _b_ cannot move along the same
      edge in the opposite direction. We call this an _edge conflict_.
2. The cumulative cost of all paths is minimal.

The naive approach to solving MAPF instances is to use a single-agent graph-search algorithm such as A*, joining the
agents into a single entity. The issue with this solution is that the search space grows exponentially with the number
of agents. Conflict-based search improves this by splitting the search into a high-level constraint tree search and a
single-agent low-level search.
This way, we leverage the reasonable efficiency of A*-based single-agent solvers.
Each node of the high-level constraint tree contains a solution candidate (set of paths for the agents), and a set of
vertex/edge constraints. The goal is to find a node where no two paths collide.

First, the root node is constructed. Since constraints are yet to be generated, a solution for the root node is found by
invoking the low-level solver separately for every agent. The paths found are guaranteed to be optimal in isolation,
but are likely not disjoint. Then, paths are validated against each other by iterating over timesteps and checking the
conflict rules described above.
When a conflict is found, there are two ways to resolve it: forbidding agent A from entering that specific node at that
specific timestep, and vice versa for agent B.
To ensure optimality, we need to inspect both cases.
So, we split the constraint tree node into two children - one for each 'side' of the conflict.
When leaving a node, the next one is selected using a priority queue in a min-cost-first manner.

## Performance

The presented implementation is able to solve most MovingAI maps with the associated benchmarks reduced to about 50
agents per map within seconds.
Full benchmarks (i.e. all 1000 agents on the Berlin256 map) take significantly more time and would probably require
further optimizations
in the form of agent merging (MA-CBS [1]) or pairwise symmetry resolving [2], but these are out of the scope of this
implementation.

When profiling the application on large instances, the clear leader in CPU time is the low-level solver,
more specifically its priority queue operations.
The total time could therefore be reduced by limiting the number of evaluated nodes of the high-level tree, which would
make the low-level search run fewer times in total.

Though not supported by rigorous testing, running some of the MovingAI benchmarks,
we can observe better performance for maps with many obstacles, such as the _random64x64_ map, compared to
the open space city maps _Berlin_, _Boston_ and _Paris_. This matches the A* vs. CBS comparison described in [1].

## Components

- The `domain` module contains definitions of domain entities like `Graph` and `Agent`
- The `parser` module contains parser classes for [MovingAI.com](https://movingai.com) problem specifications
- The `solver` module: both levels of the solver, implemented based on [1]
- The `cli` module provider a command line interface to the solver.
- The `visualiser` module contains a basic solution visualiser along with an incomplete GUI for MAPF instance
  specification.

## Running

Run `./gradlew build` to build the app. An archive with the cli executable will be places into `./cli/build/`.
`cli --help` prints usage info, `cli MAP_FILE BENCHMARK_FILE` parses MovingAI specification files and runs the solver.
Use the `-g` flag for graphics output.

## References

[1]: Sharon, G.; Stern, R.; Felner, A.; et al. Conflict-based search for optimal multi-agent pathfinding.
Artificial Intelligence, volume 219, 2015: pp. 40–66.
Available at: https://doi.org/10.1016/j.artint.2014.11.006

[2]: Li, Jiaoyang and Harabor, Daniel and Stuckey, Peter J. and Koenig, Sven;
Pairwise Symmetry Reasoning for Multi-Agent Path Finding Search. arXiv, 2021.
Available at: https://doi.org/10.48550/arXiv.2103.07116