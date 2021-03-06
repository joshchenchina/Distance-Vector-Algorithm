import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nortondj on 4/27/17.
 */
public class RouterTest {
    @Test
    public void recalculateDistanceVectorInitOthers() throws Exception {
        Router r = RouterFactory.makeRouter("resources/test1.txt", false);
        DistanceVectorCalculation result = r.recalculateDistanceVector();
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        Integer expectedWeight1 = 1;
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        Integer expectedWeight2 = 2;
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);
        Integer expectedWeight3 = 3;
        DistanceVector resultVec = result.getResultVector();
        HashMap<SocketAddress, ArrayList<SocketAddress>> pathMap = result.getPathMap();
        Assert.assertEquals(expectedWeight1, resultVec.getValue(n1));
        Assert.assertEquals(expectedWeight2, resultVec.getValue(n2));
        Assert.assertEquals(expectedWeight3, resultVec.getValue(n3));
        r.close();
    }

    @Test
    public void recalculateDistanceVectorInitSelf() throws Exception {
        Router r = RouterFactory.makeRouter("resources/test1.txt", false);
        DistanceVectorCalculation result = r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        DistanceVector resultVec = result.getResultVector();
        HashMap<SocketAddress, ArrayList<SocketAddress>> pathMap = result.getPathMap();
        Assert.assertEquals(new Integer(0), resultVec.getValue(source));
        r.close();
    }

    @Test
    public void findDistance(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);

        //from test2.txt, source -> n1,n2,n3 = (2,7,20)
        //create a vector from n1, that has n1 -> n3 = 5

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n3,5);

        //from n1 -> n1, we expect 0 by definition
        Assert.assertEquals(new Integer(0), r.findDistance(n1,n1,n1vector));
        //from n1 -> n2, we expect inf, due to unconnected
        Assert.assertEquals(new Integer(16), r.findDistance(n1,n2,n1vector));
        //from n1 -> n3, we expect 5, by value
        Assert.assertEquals(new Integer(5), r.findDistance(n1,n3,n1vector));
    }

    @Test
    public void receiveDistanceVectorCorrectness1(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);

        //from test2.txt, source -> n1,n2,n3 = (2,7,20)
        //create a vector from n1, that has n1 -> n3 = 6
        //will cause source -> n1,n2,n3 = (2, 7, 8)

        Integer expectedWeight1 = new Integer(2);
        Integer expectedWeight2 = new Integer(7);
        Integer expectedWeight3 = new Integer(8);

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n3,6);
        r.receiveDistanceVector(n1vector);
        DistanceVectorCalculation result = r.getMostRecentCalculation();
        DistanceVector resultVec = result.getResultVector();
        HashMap<SocketAddress, ArrayList<SocketAddress>> pathMap = result.getPathMap();

        Assert.assertEquals(expectedWeight1, resultVec.getValue(n1));
        Assert.assertEquals(expectedWeight2, resultVec.getValue(n2));
        Assert.assertEquals(expectedWeight3, resultVec.getValue(n3));
        r.close();
    }

    @Test
    public void receiveDistanceVectorCorrectness2(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);

        //from test2.txt, source -> n1,n2,n3 = (2,7,20)
        //create a vector from n1, that has n1 -> n3 = 7
        //will cause source -> n1,n2,n3 = (2, 7, 9)

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n3,7);

        r.receiveDistanceVector(n1vector);

        //create a vector from n2, that has n2 -> n3 = 1
        //will cause source -> n1,n2,n3 = (2, 7, 8)

        DistanceVector n2vector = new DistanceVector(n2);
        n2vector.addValue(n3, 1);

        r.receiveDistanceVector(n2vector);

        Integer expectedWeight1 = new Integer(2);
        Integer expectedWeight2 = new Integer(7);
        Integer expectedWeight3 = new Integer(8);

        DistanceVectorCalculation result = r.getMostRecentCalculation();
        DistanceVector resultVec = result.getResultVector();
        HashMap<SocketAddress, ArrayList<SocketAddress>> pathMap = result.getPathMap();

        Assert.assertEquals(expectedWeight1, resultVec.getValue(n1));
        Assert.assertEquals(expectedWeight2, resultVec.getValue(n2));
        Assert.assertEquals(expectedWeight3, resultVec.getValue(n3));
        r.close();
    }

    @Test
    public void receiveDistanceVectorAddsNewNodes(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);
        SocketAddress n4 = new SocketAddress("127.0.0.1", 11111);

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n4,7);

        r.receiveDistanceVector(n1vector);

        Assert.assertEquals(9, r.getDistanceVectorWeight(n4));
        r.close();
    }

    @Test
    public void receiveDistanceVectorCausesBroadCast(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);

        //from test2.txt, source -> n1,n2,n3 = (2,7,20)
        //create a vector from n1, that has n1 -> n3 = 7
        //will cause source -> n1,n2,n3 = (2, 7, 9)

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n3,7);

        Assert.assertTrue(r.receiveDistanceVector(n1vector));
        r.close();
    }

    @Test
    public void receiveDistanceVectorNotCausesBroadCast(){
        Router r = RouterFactory.makeRouter("resources/test2.txt", false);
        r.recalculateDistanceVector();
        SocketAddress source = new SocketAddress("127.0.0.1", 9876);
        SocketAddress n1 = new SocketAddress("127.0.0.1", 9877);
        SocketAddress n2 = new SocketAddress("127.0.0.1", 9878);
        SocketAddress n3 = new SocketAddress("127.0.0.1", 9879);

        //from test2.txt, source -> n1,n2,n3 = (2,7,20)
        //create a vector from n1, that has n1 -> n3 = 7
        //will cause source -> n1,n2,n3 = (2, 7, 9)

        DistanceVector n1vector = new DistanceVector(n1);
        n1vector.addValue(n3,10000);

        Assert.assertFalse(r.receiveDistanceVector(n1vector));
        r.close();
    }

}