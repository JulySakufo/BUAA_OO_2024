import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;
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
        int testNum = 10;//测试次数,可根据需求调整
        
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
                try {
                    myNetwork.addRelation(id1, id2, 1);
                    myShadowNetwork.addRelation(id1, id2, 1);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                
                }
            }
            object[i] = new Object[]{myNetwork, myShadowNetwork};
        }
        return Arrays.asList(object);
    }
    
    @Test
    public void testQueryTripleSum() {
        int mySum = 0;
        int sum = 0;
        Person[] persons = myNetwork.getPersons();
        Person[] shadowPersons = myShadowNetwork.getPersons();
        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++) {
                for (int k = j + 1; k < persons.length; k++) {
                    if (myNetwork.getPerson(persons[i].getId()).isLinked(myNetwork.getPerson(persons[j].getId()))) {
                        if (myNetwork.getPerson(persons[j].getId()).isLinked(myNetwork.getPerson(persons[k].getId()))) {
                            if (myNetwork.getPerson(persons[k].getId()).isLinked(myNetwork.getPerson(persons[i].getId()))) {
                                sum++;
                            }
                        }
                    }
                }
            }
        } //使用jml规格计算sum
        boolean exception = false;
        try {
            mySum = myNetwork.queryTripleSum(); //使用它的方法计算mySum
        } catch (Exception e) {
            exception = true;
        }
        assertEquals(false, exception);
        persons = myNetwork.getPersons(); //更新
        assertEquals(persons.length,shadowPersons.length); //先比较长度，看是否加了人
        for (int i = 0; i < persons.length; i++) {
            boolean flag = ((MyPerson) persons[i]).strictEquals(shadowPersons[i]);
            assertEquals(true, flag);
        } //状态都相同
        assertEquals(mySum, sum);
    }
    
}
