import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.*;

@RunWith(Parameterized.class)
public class MyTest {
    private MyNetwork myNetwork;
    private MyNetwork myShadowNetwork;
    
    public MyTest(MyNetwork myNetwork, MyNetwork myShadowNetwork) {
        this.myNetwork = myNetwork;
        this.myShadowNetwork = myShadowNetwork;
    }
    
    @Parameters
    public static Collection prepareData() {
        Random random = new Random();
        int testNum = 200;//测试次数,可根据需求调整
        
        //该二维数组的类型必须是Object类型的
        //该二维数组中的第一维代表有多少组测试数据,有多少次测试就会创造多少个PathTest对象
        //该二维数组的第二维代表PathTest构造方法中的参数，位置一一对应
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            MyNetwork myNetwork = new MyNetwork();
            MyNetwork myShadowNetwork = new MyNetwork();
            for (int j = 1; j <= 100; j++) {
                int id = j;
                String name = String.valueOf(j);
                int age = j;
                try {
                    myNetwork.addPerson(new MyPerson(id, name, age));
                    myShadowNetwork.addPerson(new MyPerson(id, name, age));
                } catch (EqualPersonIdException e) {
                    e.print();
                }
            }
            for (int j = 0; j < 300; j++) {
                int id1 = random.nextInt(100) + 1;
                int id2 = random.nextInt(100) + 1;
                int value = random.nextInt(10) + 1;
                try {
                    myNetwork.addRelation(id1, id2, value);
                    myShadowNetwork.addRelation(id1, id2, value);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                
                }
            }
            object[i] = new Object[]{myNetwork, myShadowNetwork};
        }
        return Arrays.asList(object);
    }
    
    @Test
    public void testQueryCoupleSum() {
        int mySum = 0;
        int sum = 0;
        Person[] persons = myNetwork.getPersons();
        Person[] shadowPerson = myShadowNetwork.getPersons();
        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++) {
                int id1 = 0;
                int id2 = 1;
                try {
                    id1 = myNetwork.queryBestAcquaintance(persons[i].getId());
                } catch (Exception e) {
                
                }
                try {
                    id2 = myNetwork.queryBestAcquaintance(persons[j].getId());
                } catch (Exception e) {
                
                }
                if (id1 == persons[j].getId() && id2 == persons[i].getId()) {
                    sum++;
                }
            }
        }
        mySum = myNetwork.queryCoupleSum();
        persons = myNetwork.getPersons();
        assertEquals(persons.length, shadowPerson.length);
        for (int i = 0; i < persons.length; i++) {
            boolean flag = ((MyPerson) persons[i]).strictEquals(shadowPerson[i]);
            assertEquals(true, flag);
        }
        System.out.println(mySum);
        assertEquals(mySum, sum);
    }
    
}
