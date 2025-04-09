//> using dep org.scalameta::munit:1.1.0

class TestSuite extends munit.FunSuite {

    /**
      * SomeStore tests
      * ===============
      *  
      * We are testing the functionality of SomeStore here. 
      * For the tests below, we can even introduce property based testing using generators if we wanted to.
      */

    test("SomeStore initiates with an empty state") {
        // given
        val underTest = new SomeStoreImpl

        // when and then
        assertEquals(underTest.state, List.empty)
    }
  
    test("putNumbersIntoStore: put things correctly into store") {
        // given
        val underTest = new SomeStoreImpl

        // when and then
        val _ = underTest.putNumbersIntoStore(List(1, 2, 3))
        assertEquals(underTest.state, List(1, 2, 3))

        // when and then again
        val _ = underTest.putNumbersIntoStore(List(4, 5, 6))
        assertEquals(underTest.state, List(1, 2, 3, 4, 5, 6))

        // and to test 'getCurrentState' works
        assertEquals(underTest.getCurrentState, List(1, 2, 3, 4, 5, 6))
    }

    test("someReallyComplicatedFunction: does things correctly to things in the store") {
        // given
        val underTest = new SomeStoreImpl
        val _ = underTest.putNumbersIntoStore(List(1, 2, 3))
        assertEquals(underTest.state, List(1, 2, 3))
        
        // when and then
        val _ = underTest.someComplicatedFunction
        assertEquals(underTest.state, List(2, 4, 6))

        // when and then again
        val _ = underTest.someComplicatedFunction
        assertEquals(underTest.state, List(4, 8, 12))

        // and to test 'getCurrentState' works
        assertEquals(underTest.getCurrentState, List(4, 8, 12))
    }

        test("someSuperComplexFunctionThenReturnState: does things, then returns state") {
        // given
        val underTest = new SomeStoreImpl
        val _ = underTest.putNumbersIntoStore(List(1, 2, 3))
        assertEquals(underTest.state, List(1, 2, 3))
        
        // when and then
        val res1 = underTest.someSuperComplexFunctionThenReturnState
        assertEquals(underTest.state, List(2, 4, 6))

        // when and then again
        val res2 = underTest.someSuperComplexFunctionThenReturnState
        assertEquals(underTest.state, List(4, 8, 12))
    }


    /**
      * CliTool tests
      * =============
      *  
      * We are testing the functionality of CliTool here. 
      * To test the functionaily of `printCurrentState`, we want to mock out dependencies, in this case `SomeStore`
      * so that we can easily test how the `CliTool` functions. 
      */

    /**
      * Lets begin by using a real implementation here instead of mocking. 
      * Nothing too bad so far.
      */
    test("doSomethingComplexAndPrint returns expected message when state is empty") {
        // given
        val underTest = new CliTool(new SomeStoreImpl)

        // when
        val res = new java.io.ByteArrayOutputStream()
        Console.withOut(res) {
          underTest.doSomethingComplexAndPrint
        }
        
        // then
        assertEquals(res.toString.trim(), "The response is empty")
    }

    /**
      * Lets continue using a real implementation here instead of mocking. 
      * We can start to see how our test here is coupled with `SomeStore`. 
      * i.e. Our test is now dependant on how `SomeStore` functions.
      */
    test("doSomethingComplexAndPrint returns expected message when state is not empty") {
        // given
        val dummyStore = new SomeStoreImpl
        val _ = dummyStore.putNumbersIntoStore(List(1, 2, 3))
        
        val underTest = new CliTool(dummyStore)

        // when
        val res = new java.io.ByteArrayOutputStream()
        Console.withOut(res) {
          underTest.doSomethingComplexAndPrint
        }
        
        // then
        assertEquals(res.toString.trim(), "The response is: List(2, 4, 6)")

        // our assertion here is less predictable. We are also testing how the real `SomeStore` functions, not just the `CliTool`
    }

    /**
      * Lets use an abstraction by creating a mock `SomeStore`, instead of using the real implementation.
      */
    test("doSomethingComplexAndPrint returns expected message when state is not empty, using a mocked implementation") {
        // given
        val dummyStore = new SomeStore {
          var state: List[Int] = List(111, 222, 333)
          def putNumbersIntoStore(numbers: List[Int]): Unit = ()
          def getCurrentState: List[Int] = List(111, 222, 333)
          def someComplicatedFunction: Unit = ()
          def someSuperComplexFunctionThenReturnState: List[Int] = List(222, 333, 444)
        }

        val underTest = new CliTool(dummyStore)

        // when
        val res = new java.io.ByteArrayOutputStream()
        Console.withOut(res) {
          underTest.doSomethingComplexAndPrint
        }
        
        // then
        assertEquals(res.toString.trim(), "The response is: List(222, 333, 444)")

        // our assertion here is much more predictable. We are no longer testing "SomeStore".
        // By doing this, we have decoupled the functionailty of `SomeStore` from our test.
        // We are now testing `CliTool` only. 
    }

    /**
      * Ok, we have a mock implementation of `SomeStore`. But currently it doesn't function like the real one.
      * So let's make our mock implementation more like the real one...
      */
    test("doSomethingComplexAndPrint returns expected message when state is not empty, using a mocked implementation that behaves more like the real one") {
        
        // given

        /** This is a copy and paste of `SomeStoreImpl`. 
          * This scenario is somewhat equivilent to the mocked `Store` we are talking about in the PR. 
          */ 

        val dummyStore = new SomeStore {
            var state: List[Int] = List.empty

            def putNumbersIntoStore(numbers: List[Int]): Unit =
                val newState = state.appendedAll(numbers)
                state = newState

            def getCurrentState: List[Int] = 
                state

            def someComplicatedFunction: Unit =
                val newState = state.map(_ * 2)
                state = newState

            def someSuperComplexFunctionThenReturnState: List[Int] = 
                val _ = someComplicatedFunction

                getCurrentState
        }

        val _ = dummyStore.putNumbersIntoStore(List(1, 2, 3))

        val underTest = new CliTool(dummyStore)

        // when
        val res = new java.io.ByteArrayOutputStream()
        Console.withOut(res) {
          underTest.doSomethingComplexAndPrint
        }
        
        // then
        assertEquals(res.toString.trim(), "The response is: List(2, 4, 6)")

        /**
          * Some questions I have here:
          * - What extra value does using a mocked implementation that behaves more like the real thing provide? In this example it's (almost) identical to `SomeStoreImpl`
          * 
          * - Perhaps in a hypothetical scenario there is some value to testing `CliTool` using a mock that is more close to the real implementation.
          * - In this case, why not just use the real thing?
          * - If we use the real thing, is this still unit testing "CliTool.doSomethingComplexAndPrint"?\
          * - Is that more an E2E or integration test?
          * 
          * - If we continue to use a "mocked" dependency that is meant to function like the real one...
          *   What is there that will verify that our heavily altered "mocked" dependency to behave like the real one, behaves just like the real one?
          */
    }
  
}