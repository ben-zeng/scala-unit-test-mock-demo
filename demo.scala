trait SomeStore {
    var state: List[Int]
    def putNumbersIntoStore(numbers: List[Int]): Unit
    def getCurrentState: List[Int]
    def someComplicatedFunction: Unit
    def someSuperComplexFunctionThenReturnState: List[Int]
}

class SomeStoreImpl extends SomeStore {

  // pretend this is private, not accessable usually from other classes, and is some sort of persistance provider.
  var state: List[Int] = List.empty

  def putNumbersIntoStore(numbers: List[Int]): Unit =
    val newState = state.appendedAll(numbers)
    state = newState

  def getCurrentState: List[Int] = 
    state

  def someComplicatedFunction: Unit =
    // In reality, this isn't so compicated but this is just a demo.
    // It just takes what is currently in 'state' and multiplies it by 2
    val newState = state.map(_ * 2)
    state = newState

  def someSuperComplexFunctionThenReturnState: List[Int] = 
    val _ = someComplicatedFunction

    getCurrentState
}

class CliTool(someStore: SomeStore) {

  def doSomethingComplexAndPrint: Unit = 
    val resFromStore = someStore.someSuperComplexFunctionThenReturnState
    
    if (resFromStore.isEmpty) println("The response is empty")
    else println(s" The response is: ${resFromStore}")

}

object Main {

    val store: SomeStore = new SomeStoreImpl
    val cli: CliTool = new CliTool(store)
    
    // This could be a server, or a CLI app
    // Currently for demo purposes it just returns what's in the state on each invocation
    def main(args: Array[String]): Unit =
        cli.doSomethingComplexAndPrint

}


