#Grunner
A remote Groovy Script batch execution service. 

_Batch: a computer operation whose expected execution time is expected to exceed the user's willingness to wait for it to finish._

**NOT SECURED FOR PRODUCTION**
---

# Features
_Grunner_ is an HTTP service that allows sending Groovy scripts to a remote server for batch execution.
As the service runs the scripts, results are preserved for later retrieval.   

As of now, Grunner supports four operations:
- Enqueue a new batch
- List all batches status, with optional status filter
- Cancel a batch 
- Retrieve a batch's results

Batches can be in five states 
- Pending
- Executing
- Completed
- Cancelled
- Failed
 
Batches are created with the `Pending` state.
Each new batch is attributed a unique handle which is returned to the submitter for further operations.  
New batches execute immediately unless the execution pipeline is full.
Only batches that remain `Pending` can be `Cancelled`.
As there is no execution timeout, batches can stay pending indefinitely.

After `Executing`, batches are marked as either `Completed` or `Failed`.
Batch results are in text format and capture the script's return value or error details. 
Once finished, batches are never deleted or retried.

## Disclaimer 
As boldly outlined at the top of this README, this service is eminently unsafe 
as it allows anybody on the Internet to execute arbitrary code on the machine it runs on.
Any intrusion mitigation system it may have (hint: it has _none_) 
is highly likely to be flawed, in a delightfully subtle or painfully obvious way.
   
Hence, absolutely no claims of security are done for the provided software 
and you shall be solely responsible for any mayhem that may occur from its use.  

## Requirements
The following tools are required to build and run this application:
- Java 14
- Maven 3.6
- Docker (Optional)

No binary releases are provided.

This software has been developed and tested on Linux but should work on any machine with the above tools installed.
OS-specific documentation only covers Linux, sorry.  
If you encounter any problem or have a suggestion, please create a bitbucket issue to report it. 

## Compiling
First, compile the application.
After cloning the source code, from the root of the project, at the command line, type
```
mvn package
```
This will, in order:
- generate code for the API and Database layers
- compile everything
- run the unit tests 
- produce a `grunner.jar` file in the `target` sub folder  

Optionally, to perform the above steps and then additionally run the integration tests, type:
```
mvn verify
```
Running the integrated test suite takes long and creates a test database on the disk.  

## Launching
Once the service is compiled (see previous section), at the command line, type
```
java -jar target/grunner.jar
```
There are no command-line options.
Startup takes ~7 seconds on first generation 2.8GHz Core i7.  
Once launched, the service will be on port 8080 (unsecured http). 

## Operation

In additon to a HTTP REST API for programmatic control, 
the service is human-accessible using the built-in Swagger Web UI.
The Swagger UI is now yours to explore and should be accessible at
```
http://localhost:8080/
```

Each REST exchange performed in the UI also produces a sample CLI command.
These commands can be helpful when composing execution scripts.


All usage of the service's interfaces require a login through HTTP Basic auth. 
The baked-in usernames are `user1` and `user2` with their respective password both being `secret` (how original).
Batches and their results are _owned_ by the user who created them and are only accessible from that user's account.   
Otherwise, logging in gives full access to all operations.

As the state of the service is backed by a database it is persistent, 
meaning that scripts, results and errors will not be lost across server restarts. 
 
## Design
Grunner is a primarily a Spring Boot WebMVC application.

It has been designed to minimize writing custom config and code.
Following the contract-first philosophy, it makes heavy use of code generators:
- OpenAPI codegen for the REST API layer
- Flyway for SQL database schema creation and migration
- JOOQ for runtime SQL database access

Code generation is performed at build time before compilation, thus:
- Errors from generated code appear earlier in the development process
- Runtime impact is minimal 
 
Having authoritative models in a language specific to their target layer (OpenAPI, SQL) 
- Helps reflect the true intent of the developer 
- Allows finer navigation of the layer's intricacies, for performance and workarounds

Finally, fewer remaining hand-coded classes focus on application specific behavior and layer integration, 
reducing overall depth and complexity and facilitating correctness validation and long term maintenance.  

Some possible disadvantages of this approach are:
- Harder initial developer contact (multiple languages, more complex project structure)
- Somewhat longer build times
- Dependence on the continued support of organizations providing the code generators 

## Future
Grunner could be made better with the implementation of some features such as:
- Sandboxing of script execution.
    This was not pursued because it was tricky to get right within the project time frame. 
    Also, unproven security provides a false sense of security.
- Time and quotas on batch execution 
- Pending batch timeouts
- Batch retries, with max try count
- Unlimited, streaming batch results through WebSocket API
- Administrative functions to handle users and their batches
- Periodic cleanup of finished batches
- Other scripting languages
- Batch input parameters and saved model batches
- Allow retrieving a batch script after it was submitted 

## End 
Thank you for reading.