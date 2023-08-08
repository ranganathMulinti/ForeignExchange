clone the project from command terminal using git clone command Ex: git clone https://github.com/ranganathMulinti/ForeignExchange.git
or from IntelliJ go to File -> New -> project From Version Control and paste the https or ssh git link

make sure to install java 1.8 and select in File -> Project Structure -> project -> SDK
install scala plugin from File -> Settings -> plugins

make sure the project is recognized as maven project, if not right-click on the pom.xml file and select the option Add as maven file

to build jar, run "mvn clean install" command on your terminal, this will create fx-order-matching-engine-1.0-SNAPSHOT-jar-with-dependencies.jar

FxOrdersMatchingEngine is the main application where we read data from csv file, create buy orders and sell orders dataframe and find out the matching orders

In order to run FxOrdersMatchingEngine in your local, you can directly navigate to FxOrdersMatchingEngine object and click on run icon

Below is the spark-submit command to run the job in cluster, here I am assuming yarn cluster

spark-submit \
--class com.example.FxOrdersMatchingEngine \
--name "Foreign Exchange Order Matching Engine" \
--master yarn \
--num-executors 2 \
--driver-memory 2G \
--executor-memory 2G \
--executor-cores 4 \
<path-to-jar>/fx-order-matching-engine-1.0-SNAPSHOT-jar-with-dependencies.jar

in the above spark-submit command, fx-order-matching-engine-1.0-SNAPSHOT-jar-with-dependencies.jar contains all the dependency jars, so no need to externally specify the jars
if the dependency jars are not included then we need to specify the jars using --jars options
if we need to pass any input arguments to the application, we can pass the arguments at the end with space separated

num-executor, executor-memory and executor-cores can be decided based on the amount of data the application is handling
or we can set spark dynamic allocation to true, to let spark decide