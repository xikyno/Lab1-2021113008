import org.junit.Test;

public class Lab1Test {

    @Test
    public void testReadFileAndCreateGraph() {
        Lab1.readFileAndCreateGraph();
    }
    @Test
    public void testCalcShortestPath() {

        System.out.println(Lab1.calcShortestPath( "hampshire" , "and"));
    }


}