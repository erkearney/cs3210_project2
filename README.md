# cs3210_project2
If you actually read this tell me in Slack.

## How evaluating funcCall works:
When parser does its thing, it creates a funcDefs Node:
<program> -> <funcCall> | <funcCall> <funcDefs>
The funcDefs Node will hold ALL the function definitions, it'll look like this:
<funcDefs> -> <funcDef> <funcDefs>
                        <funcDefs> -> <funcDef> <funcDefs>
                                                etc, until eventually ...
                                                <funcDefs> -> <funcDef>

When we evaluate a funcCall, we have to search through all the funcDefs until
we find the funcDef whose name matches the funcCall. We search through the 
funcDefs by creating a pointer node, called checkFunction, and pointing it
to the very first funcDefs node. We then compare checkFunction.first.info,
which will the the function name of the <funcDef>. If it's not the function
we're looking for, we set checkFunction to checkFunction.second, which will
be the next <funcDefs> node, and repeat.
