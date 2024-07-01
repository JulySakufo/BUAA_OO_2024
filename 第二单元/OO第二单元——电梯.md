# OO第二单元——电梯

## 第一次作业

本次作业中电梯的行为为到达某一位置，开门，关门。乘客的行为为进入电梯与离开电梯。在本次作业中，每个乘客的乘坐请求都指定了对应的电梯，电梯的各项性能参数也是固定不变的，十分基础。



### UML类图

<img src="D:\OO\第二单元\oohomework_2024_22371500_hw_5\hw5_graph.png" alt="hw5_graph" style="zoom: 25%;" />



### UML协作图

![hw5_UML](D:\OO\第二单元\oohomework_2024_22371500_hw_5\hw5_UML.png)



### 代码架构分析

#### 行为集合 ACTION

采用枚举的方式，使代码具有更高的可读性和逻辑。

```java
public enum Action { //表示电梯接下来的行为
    OVER,MOVE,OPEN,REVERSE,WAIT
}
```



#### 调度器 Dispatcher

在我的实现中，调度器负责接受乘客信息的输入，并且完成乘客具体信息的解析，解析完毕后将该乘客用`Person`实例化后加入到指定电梯的侯乘表中，同时用`notifyAll()`方法唤醒可能处于`ACTION.WAIT`状态的电梯，保障多线程电梯的顺利进行。

调度器中拥有6部电梯的侯乘表，有利于在`dispatcher`直接进行分发请求操作。这也是调度器线程与电梯线程进行交互的方式。

```java
//Dispatcher.java    
public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest request = elevatorInput.nextPersonRequest(); //乘客信息
            if (request == null) { //输入结束
                for (RequestTable requestTable : totalRequestTable) {
                    requestTable.setEndFlag(true);
                }
                break; //结束输入流线程
            } else {
                /*...*/
                totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);//新增生产，唤醒可能沉睡的电梯
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```



#### 侯乘表 RequestTable——关键共享对象

侯乘表中用`HashMap<Integer,ArrayList<Person>>`结构保存了乘客的信息。以出发楼层作为索引，记录该楼层能上电梯的全部乘客信息。同时还有一个布尔类型的变量`endFlag`用来标志着调度器的输入是否结束。

`RequestTable`是一个共享对象，它由`Dispatcher`线程和`Elevator`线程共同拥有操作，需要时刻保障线程安全。

```java
//RequestTable.java
//key method1:
public synchronized void addPerson(int fromFloor, Person person) {
        if (!requestMap.containsKey(fromFloor)) {
            ArrayList<Person> arrayList = new ArrayList<>();
            arrayList.add(person);
            requestMap.put(fromFloor, arrayList);
        } else {
            ArrayList<Person> arrayList = requestMap.get(fromFloor);
            arrayList.add(person);
        }
        notifyAll(); //生产了，唤醒可能沉睡的
    }

//key method2:
public synchronized Person removePerson(int curFloor, boolean direction) {
        ArrayList<Person> arrayList = requestMap.get(curFloor);
        if (arrayList == null) {
            notifyAll();
            return null;
        }
        if (direction) { //小的先上
            arrayList.sort(Comparator.comparingInt(Person::getToFloor));
        } else { //大的先下
            arrayList.sort((a, b) -> b.getToFloor() - a.getToFloor());
        }
        if (!arrayList.isEmpty()) {
            for (Person person : arrayList) {
                if (person.getDirection() == direction) {
                    arrayList.remove(person); //只删除一次，不用迭代器
                    if (arrayList.isEmpty()) { //这个人被除去了
                        requestMap.remove(curFloor);//除去键值对
                    }
                    notifyAll();
                    return person;
                }
            }
        }
        notifyAll();
        return null;
    }
```



#### 电梯 Elevator

我的实现中并没有将电梯与电梯线程分离，而是直接把电梯作为了一个线程。我一开始的想法就很简单，尽可能模拟生活中电梯的行为，因为存在即合理，日常生活中电梯为什么这样运转一定有它的道理，因此我的调度策略也是基于生活中的情形进行调度的，发现这其实与LOOK算法非常吻合。

我采用的调度策略：

- 判断是否能下人：电梯里的人若有能在该层下的，开门，放人
- 判断是否能上人：电梯未满，且想要上电梯的人的请求方向与电梯方向运行一致，那么乘客进入电梯
- 判断是否转向：如果电梯空了，且如果电梯此时向上，所有乘客的`fromFloor`皆低于电梯现在所在楼层，或者电梯此时向下，所有乘客的`fromFloor`皆高于电梯现在所在楼层，那么电梯调转方向。
- 判断是否可以原地等待：如果侯乘表空，电梯为空，且`!endFlag`，那么调用`requestTable.wait()`，间接地让电梯歇着。有新的乘客请求就通过`requestTable.addPerson()`方法唤醒侯乘表，间接唤醒电梯，从而开始工作。
- 判断是否可以结束：如果侯乘表为空，电梯为空，且`endFlag`，那么结束电梯线程。

我将上述逻辑全部封装在`Elevator`中，作为电梯的运行逻辑。有一点要注意的是，电梯执行先下后上的逻辑，因此用`ACTION.OPEN`表示是否能开门，开门后再判断是能执行上人还是下人的操作。

```java
//Elevator.java
public void run() { //具体行动
        while (true) {
            Action action = getAction();
            if (action == Action.OVER) {
                break;//结束线程
            } else if (action == Action.OPEN) { //电梯开门
                open();
            } else if (action == Action.REVERSE) {
                reverse();
            } else if (action == Action.MOVE) {
                move();
            } else {
                elevatorWait();
            }
        }
    }

public Action getAction() { //接下来要做什么行动
        if (canOut() || canIn()) { //有人上或下是电梯开门与否的条件
            return Action.OPEN;
        } else if (canWait()) {
            return Action.WAIT;
        } else if (canReverse()) {
            return Action.REVERSE;
        } else if (canOver()) {
            return Action.OVER;
        }
        return Action.MOVE; //上述情况均不满足，则默认需要移动
    }
```



### 同步块设置和锁的选择

本次作业中，为了保障线程安全，我采用的方法是将所有与共享对象有关的方法都用`synchronized`修饰，保证只有一个线程能够进行共享对象的操作。除此之外，并没有别的需要进行锁的操作。



### BUG、DEBUG与HACK

- 自我debug

本次作业中，我在互测被刀了2次，都是因为出现了`ConCurrentModificationException`问题，经过报错信息提示，我定位到出现问题的地方，发现是我在电梯线程中取得`RequestTable`后，未对其进行上锁处理，这也就导致了虽然电梯线程在进行从侯乘表删除人的操作，但是调度器线程同时也在从输入读取请求并且加入到侯乘表中，导致前后的`hashMap`大小不一致从而出现报错。发现这bug还是挺容易的，光看报错信息就知道为啥了。

- Hack

本次互测中除我之外没有人有bug...最无痛的一集。



## 第二次作业

本次作业新增了电梯重置请求，该请求会改变电梯的容纳乘客量、移动一层的时间两个参数。此外，不再指定电梯，需要自己设置合适的调度方案以实现乘客的运输。为了比较策略不同性能的差异，我一开始的打算就是做完随机、平均基础版本后，再参考往届影子电梯进行最大程度的优化。但不幸的是，在实现影子电梯的时候，通过借用同学的评测机我发现了自己仍存在0.1%超时死锁的问题，经过排查，发现自己第一次作业实际上也存在着死锁的情况。于是de了一天bug，最后成功解决了死锁问题，几乎可以说是百分百保障了正确性，但也因此没有时间再写影子电梯了，所以经过衡量，最终交了平均版上去。

平均版和随机版的调度策略实现起来都很简单，在原来的基础上删去指定的侯乘表，写`random`函数或是用一个自增的变量就行了。



### UML类图

<img src="D:\OO\第二单元\oohomework_2024_22371500_hw_6\hw6_graph.png" alt="hw6_graph" style="zoom: 25%;" />



### UML协作图

![hw6_UML](D:\OO\第二单元\oohomework_2024_22371500_hw_6\hw6_UML.png)





### 代码架构分析

对比我的第一次和第二次的UML图发现其实并没有什么变化，只是`Elevator`里面多了一个`ACTION.RESET`的操作而已。我想实现的也正是这种新增的需求可以通过之前已有的架构增加的方式，只需要新增很少的代码，对于原来已有的代码基本或做很少的修改，让其具有良好的迭代性。



#### 调度器 Dispatcher

正如上文所提到的，最后我采用的是平均的策略，因此在调度器中只有微量的修改。新增count实现平均分配给电梯，防止某些电梯分到的请求数量远大于其他电梯，这也是生活中电梯运行经常出现的。你也不会看见用一部电梯接受所有请求吧。

```java
//Dispatcher.java
private int count;
public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest(); //乘客信息
            if (request == null) { //输入结束
                for (RequestTable requestTable : totalRequestTable) {
                    requestTable.setEndFlag(true);
                }
                break; //结束输入流线程
            } else {
                if (request instanceof PersonRequest) {
                    int elevatorId = count % 6 + 1;
                    synchronized (totalRequestTable.get(elevatorId - 1)) {
                        totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);
                        TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId);
                    }
                    count = (count + 1) % 6;
                } else if (request instanceof ResetRequest) {
                    int elevatorId = ((ResetRequest) request).getElevatorId();
                    totalRequestTable.get(elevatorId - 1).addReset((ResetRequest) request);
                }
                
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```



#### 侯乘表 RequestTable

因为新增了`reset`请求，所以自然的想法就是仿照`PersonRequest`在侯乘表中也放一个`reset`的表，用来记录收到的重置情况。

```java
//RequestTable.java
private final ArrayList<ResetRequest> reset;
public synchronized void addReset(ResetRequest resetRequest) {
        reset.add(resetRequest);
        notifyAll();
    }

public synchronized ResetRequest removeResetRequest() {
        notifyAll();
        return reset.remove(0);
    }
```



#### 电梯 Elevator

我在实现`reset`的过程中是将`reset`也当作与`open`，`move`等行为逻辑，直接在`reset`里完成所有电梯相关重置请求，而不只完成一部分，另一部分又由下一个时间段完成，通过与同学的聊天发现，他们的方式似乎很复杂，而我写`reset`逻辑觉得很快就写完了。

课程组要求尽快实现`reset`操作，所以判断是否要进行`reset`操作只需要看`requestTable`的`reset`是否为空就行了，不为空，那就立即执行`reset`操作，在写的时候我注意到了`reset`似乎具有相当高的优先级，于是将其提到最前面，只要可以`reset`就进行`reset`，保证不会进入其他操作而延迟`reset`。

判断完是否进行`reset`的条件后，只需要按部就班，规矩地翻译`reset`的行为就行了。在这里，我将乘客放下后并没有重新分配给其他电梯，考虑到重置请求是非常快的，根据生活实际情况，感觉也没有必要再发给其他电梯了。所以采用原地放下，等重置完毕后再让这些乘客重新登上电梯即可。

```java
public void run() { //具体行动
        while (true) {
            synchronized (requestTable) {
                Action action = getAction();
                action1 = action;
                if (action == Action.WAIT) {
                    elevatorWait();
                }
            }
            if (action1 == Action.OVER) {
                break;//结束线程
            } else if (action1 == Action.OPEN) { //电梯开门
                elevatorOpen();
            } else if (action1 == Action.REVERSE) {
                elevatorReverse();
            } else if (action1 == Action.MOVE) {
                elevatorMove();
            } else if (action1 == Action.RESET) {
                elevatorReset();
            }
        }
    }

public Action getAction() { //接下来要做什么行动
        synchronized (requestTable) {
            if (canReset()) { //其次是重置，有重置剩下的操作不再执行而执行重置操作
                return Action.RESET;
            } else if (canOut() || canIn()) { //有人上或下是电梯开门与否的条件
                return Action.OPEN;
            } else if (canWait()) {
                return Action.WAIT;
            } else if (canReverse()) {
                return Action.REVERSE;
            } else if (canOver()) { //结束具有最低的优先级
                return Action.OVER;
            }
            return Action.MOVE; //上述情况均不满足，则默认需要移动
        }
    }

public void elevatorReset() { 
        synchronized (requestTable) { //不让dispatcher使用add添加了，只让电梯进行reset操作，表不让任何人使用
            if (!peopleMap.isEmpty()) {
                /*执行reset，将乘客原地放下*/
                }
                curPeople = 0;
                peopleMap.clear();//清空电梯里的人
                try {
                    sleep(400); //开关门持续时间为0.4s
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                TimableOutput.println("CLOSE" + "-" + curFloor + "-" + id);
            }
            ResetRequest resetRequest = requestTable.removeResetRequest();
            this.capacity = resetRequest.getCapacity();
            this.waitTime = (int) (resetRequest.getSpeed() * 1000);
            TimableOutput.println("RESET_BEGIN-" + id);
            try {
                sleep(1200); //重置时间1.2s，sleep不释放锁
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println("RESET_END-" + id);
            requestTable.printReceive(id);//先receive，表示重新receive之前分配给该电梯的请求
            //receive之前放出的，reset过程就结束了，具体的open-in-close过程不再属于reset过程，而是reset之后要做的事情
        } //执行完后自动释放锁
    }
```



### 同步块设置与锁的选择

经过Bug修复，我对`synchronized`的使用有了更深的体会，所以这次作业中只有需要才上锁，并且删除了一些不相干的`notifyAll`的冗余操作，将`synchronized`的临界区尽可能设置的小，依然是没有采取更加灵活的读写锁的形式。



### BUG、DEBUG与HACK

- BUG与自我DEBUG

本次作业中，我强测互测都没有问题，但是性能分确实垃圾。

为了避免像上次被互测测出问题，这次我跑了大量的评测机，测出了超时的问题。在不断的debug过程中，我反复执行不同位置的断点，最后确定了是`wait`后没有`notify`导致的死锁，我一开始百思不得其解，为何会这样呢？通过不断查看线程中各个成员的状态，我最后发现问题是：当`Dispatcher`想要将`requestTable`的`endFlag`设置为`true`的时候，`Elevator`线程恰好在准备执行`wait`操作，也就是说`notifyAll`的时候线程是还没有进入`wait`的，在`notifyAll`结束后才进入`wait`，之后又没有新的请求，自然电梯没有读到`endFlag`为真的条件，所以就一直`wait`下去，程序没有结束，所以也就造成了超时。发现了问题之后解决起来就很简单了，只需要使得在准备执行`wait`操作的时候，不让`Dispatcher`能够`setEndFlag`就行了，也就是说加一个`synchronized(requestTable)`同步块就ok了。修改完逻辑后，我又跑了3000组同学的强大评测机，无一超时。有趣的是，用一开始的随机版或者平均版触发bug概率都很低，这个bug还是我用影子电梯稳定复现的，某种意义上说，影子电梯也帮了我一把，虽然在后面的hack环节中我发现低于5%的死锁率基本在平台上是测不出来的(。

- HACK

强度最大的一集。

本次HACK我提交了280发。

其实不是毫无目的地提交，是因为评测机真的测不出来...首先通过评测机跑了每个人的`jar`包，初步确定了基本每个人都有问题(，于是就开始准备刀人。其实在进入互测环节之前，我就有预感这次hack次数会创记录，因为自己在本地复现自己的bug都不太好复现，这也许就是多线程的魅力吧(。

首先是经典数据"围师必阙"，在某个时刻重置5部电梯，并塞满大量请求，如果同学是让重置的电梯不接受请求，那么就会出现一个电梯运载所有乘客的现象，会严重的超时。

其次就是经典`reset`和乘客同时到来的情况，这也是我上面所de的bug，有些同学也会在程序本应该结束的时候没有结束。

最后是多个`reset`同时进行的时候，一些同学会在`reset end`后停滞不动，因为没有查看代码所以不知道具体是什么情况。我也是发现有同学本地对于该数据基本百分百卡死的情况下，提交上去，以为能够轻易地爆掉该同学，结果该同学运气出奇的好，本地几乎达到百分百爆率评测机上面30发硬是一发没中，甚至还误伤了其余两位本地基本跑不出来卡死情况的同学。

对于hack策略，其实没什么好说的，本单元hack思路其实相对单调，无非就是在边界条件上做文章，控制好时间戳，基本都能测出来很多同学的问题。只是评测机确实测不出来罢了。。。



## 第三次作业

本次作业新增双轿厢电梯请求，经过查阅资料，才知道双轿厢电梯是什么东西。让两个厢电梯实现将乘客运达目的地，想要提高电梯的运行效率。



### UML类图

<img src="D:\OO\oohomework_2024_22371500_hw_7\hw7_graph.png" alt="hw7_graph" style="zoom:25%;" />



### UML协作图

![hw7_UML](D:\OO\oohomework_2024_22371500_hw_7\hw7_UML.png)



### 代码架构分析

这次其实写的代码还挺多的，主要是一直在想有没有什么优美的方法能够解决问题。于是打算先从双轿厢开始写起，一步一步往外修改。事实证明我的想法是对的，一路上都是写代码->发现问题->修正代码->解决问题。



#### 电梯 Elevator

正如先前所说的，好的代码基本能够实现有新增需求只需要在原有代码上新增新的代码实现即可，尽量不去动原有的代码。在我的Elevator中为了实现双轿厢重置的请求，新增了判断逻辑和收到双轿厢请求后电梯如何进行下一步。双轿厢电梯的重置与普通重置类似，都是收到重置请求后尽快进行。因此我们仿照普通重置的行为即可。双轿厢电梯重置给我一种原有电梯一分为二的感觉，因此我把行为逻辑命名为elevatorSplit，感觉相当的贴切。因为是两个轿厢，且有对应的换乘站，构建对应的DCelevator即可。

```java
//Elevator.java
    public void elevatorSplit() {
        synchronized (requestTable) { //锁表，不让dispatcher再加乘客进来
            synchronized (Dispatcher.getRequestTable(id, "A")) {
                synchronized (Dispatcher.getRequestTable(id, "B")) { //所有相关的表全部锁起来
                    Dispatcher.setFlag(id);
                    RequestTable requestTable1 = Dispatcher.getRequestTable(id, "A");
                    RequestTable requestTable2 = Dispatcher.getRequestTable(id, "B");
                    putDownAll();
                   /*双轿厢电梯的初始化操作*/
                    for (Integer key : requestMap.keySet()) { //拷贝给双轿厢电梯
                        ArrayList<Person> arrayList = requestMap.get(key);
                        for (Person person : arrayList) {
                            boolean direction = person.getDirection();
                            if (direction) {
                                if (key < transFloor) {
                                    dcElevatorA.initializePerson(key, person);
                                } else {
                                    dcElevatorB.initializePerson(key, person);
                                }
                            } else {
                                if (key <= transFloor) { //将在换乘楼层及换乘楼层下的给A
                                    dcElevatorA.initializePerson(key, person);
                                } else { //在换乘楼层上的塞给B
                                    dcElevatorB.initializePerson(key, person);
                                }
                            }
                        }
                    }
  					/*输出行为逻辑*/
                    dcElevatorA.start();
                    dcElevatorB.start();
                }
            }
        }
    }
```



#### 双轿厢电梯 DCelevator

想想生活中的双轿厢电梯，它们既有自己独立工作的部分，同时也会协作完成某些工作。在我的实现中，侯乘表是个关键的成员，它直接关乎到电梯的运行。为了实现上文所提到的既能独立工作又能协作工作，我在双轿厢电梯中新增了另一个轿厢电梯的侯乘表，同时为了满足题目中两个电梯不能相撞，又设计了一个换乘楼层对象用来进行上锁，防止两个轿厢相撞。一个轿厢在换乘楼层的时候，就将这个楼层锁起来，不让另一个轿厢能够进入到这一楼层，这就很简单地实现了两座电梯不能相撞了。

双轿厢电梯与普通电梯对比来看，区别很大的行为逻辑只有运行逻辑，因为每一次运行我们都要判断是否到了换乘楼层，对于开门关门，反转逻辑我完全沿用普通电梯的行为逻辑。此外，因为双轿厢电梯互帮互助，结束条件和等待条件也与普通电梯不一样了，我们不仅需要考虑本身轿厢的侯乘表和电梯内的情况，也要考虑另一个轿厢的侯乘表和电梯内的情况。

- 什么时候能结束呢？

外部输入结束，并且本轿厢电梯内没人，且侯乘表不再有乘客请求，并且另一个轿厢也没人，另一个侯乘表也没有乘客请求，这才是结束的条件，可谓是相当复杂了，尤其是两个侯乘表之间互相锁来锁去的，如果采用`synchronized`那么就很容易出现死锁情况，因此采用了更加灵活且允许多个线程同时进行读取操作的读锁来完成目的。

- 什么时候能等待呢？
  - 外部输入没结束的时候，并且本轿厢电梯没人，且侯乘表不再有乘客请求可以等待。
  - 外部输入结束了，并且本轿厢电梯没人，侯乘表也没有乘客请求，但是另一个轿厢还有人，不能够进行结束，因为有可能还需要本轿厢协助完成运送。
- 为了避免两个轿厢相撞，行动逻辑是怎样的呢？

根据轿厢的类型，每当可以行动的时候，都判断一下是否即将到达换乘楼层，如果没有到达换乘楼层，那么行为就与之前普通的电梯一样，如果到达了换乘楼层，锁住换乘楼层，放人，再移动到之前来的那个楼层即可。这样就保证了每个轿厢电梯即使到达换乘楼层，也不会一直待在换乘楼层，同时也避免了相撞的问题。

```java
//DCelevator.java
private TransFloor transFloor;//换乘楼层
private RequestTable otherRequestTable;//另一个轿厢的侯乘表
private String type;//轿厢的类型
public boolean canWait() { //电梯里没人，并且没有新增乘坐需求，输入流说还可以等待
        readLock.lock();
        try {
            if (curPeople == 0 && requestTable.isPersonEmpty() && !requestTable.getEndFlag()) {
                return true;
            } else if (curPeople == 0 && requestTable.isPersonEmpty()
                    && requestTable.getEndFlag()) {
                if (!(otherRequestTable.isPersonEmpty() && otherRequestTable.getEndFlag()
                        && otherElevator.getCurPeople() == 0)) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

public boolean canOver() { //电梯里没人，没新增需求，输入流告知所有输入均已结束
        readLock.lock();
        try {
            if (!isWake) {
                if (curPeople == 0 && requestTable.isPersonEmpty() && requestTable.getEndFlag()) {
                    if (otherRequestTable.isPersonEmpty() && otherRequestTable.getEndFlag()) {
                        if (otherElevator.getCurPeople() == 0) {
                            otherElevator.isWake = !otherElevator.isWake;
                            otherRequestTable.wakeMeUp();
                            return true;
                        }
                    }
                }
                return false;
            }
            return curPeople == 0 && requestTable.isPersonEmpty() && requestTable.getEndFlag();
        } finally {
            readLock.unlock();
        }
    }

public void elevatorMove() {
        try {
            sleep(waitTime); //花费移动一层楼的时间
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction) {
            if (type.equals("A")) { //A电梯，且将移动到换乘楼层，先check，B电梯是否在换乘楼层
                if (curFloor + 1 == transFloor.getTransFloor()) { //换乘电梯是新的最高点
                    synchronized (transFloor) { //换乘站是共享对象，锁住
                        curFloor++;
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                        elevatorReverse();//A到达换乘站只能向下走了
                        elevatorOpen(); //到达换乘站，下人，接人
                        try {
                            sleep(waitTime); //运行一层的时间
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        curFloor--;//离开换乘站，为另一个电梯留出空间
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                    }
                } else {
                    curFloor++;
                    TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                }
            } else if (type.equals("B")) { //B电梯，向上移动无所谓
                curFloor++;
                TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
            }
        } else {
            /*类似操作*/
        }
    }
    
```



#### 调度器 Dispatcher

一开始我是想的在`split`操作后将双轿厢电梯的侯乘表加入到`Dispatcher`中去，但是后来发现好像不太好操作，在参考了`exp4_2`的流水线模式后，我采用在`Dispatcher`中放置`static`类型变量，直接先预先准备好12个双轿厢电梯的侯乘表，按需直接取用即可。什么时候能够用双轿厢电梯的侯乘表呢？为了解决这个问题，我又采用了标记数组`boolean[] flag`，当对应编号的`flag[i]`为真时，就将乘客请求加入到双轿厢电梯的侯乘表中去。感觉合理使用静态变量能起到非常大的帮助。

调度还有一点需要注意的是，当分裂成双轿厢电梯之后，对于一个新的到来的乘客，通过均分确定了应该乘坐哪个电梯，那么如果该编号电梯是双轿厢电梯，我们还要判断是该加入A电梯还是B电梯，不然又可能出现明明电梯内有乘客，却来回踱步的情况。

```java
//Dispatcher.java
public void dealPersonRequest(Request request) {
        int elevatorId = count % 6 + 1;
        //获取乘客信息
        if (lastId == elevatorId) { //条件睡眠，解决线程安全问题，因为重置请求次数很少，所以微小的sleep是可行的
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!flag[elevatorId]) { //不是双轿厢电梯
            synchronized (totalRequestTable.get(elevatorId - 1)) {
                totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);
                TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId);
            }
        } else { //是双轿厢电梯，两个表都锁上
            synchronized (DCRequestTable.get(elevatorId).get(0)) {
                synchronized (DCRequestTable.get(elevatorId).get(1)) {
                    RequestTable reqA = DCRequestTable.get(elevatorId).get(0);
                    RequestTable reqB = DCRequestTable.get(elevatorId).get(1);
                    int t = DCRequestTable.get(elevatorId).get(0).getTransFloor();
                    if (direction) { //上行请求
                        if (fromFloor < t) {
                            reqA.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-A");
                        } else {
                            reqB.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-B");
                        }
                    } else { //下行请求
                        if (fromFloor <= t) {
                            reqA.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-A");
                        } else {
                            reqB.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-B");
                        }
                    }
                }
            } //加入请求即打印，开始运行
        }
        count = (count + 1) % 6;
        lastId = -1;//用过一次就行了
    }
    
```



### 同步块设置与锁的选择

这次运用了更加灵活的读写锁，使得多个线程能够同时读取某一个共享变量，避免交互时出现互相上锁导致死锁的情况。



### BUG、DEBUG与HACK

- BUG与自我DEBUG

在经历过上一次的解决线程安全后，我对本次作业遇到的问题其实是能够大致定位的。遇到不对的数据，就在出问题的地方打断点，因为采取的是均分策略，所以问题只要复现就能够很清楚地知道到底是哪里的问题。通过单步调试操作，查看线程的各项成员状态，很清楚地就能知道哪些地方本来是应该变化的结果却没变，哪些地方应该不变的结果最后却变了。个人感觉只要能够在多线程中稳定复现问题，其实debug也还好。毕竟方法总比困难多。

- HACK

经历过上一次刀不中人的痛苦之后，这次其实很佛系。同样也是本地跑7个人的jar文件，先跑出来有哪些问题，然后手搓一些特殊的有针对性的数据扔进去即可。刀不中也真没办法，毕竟这次还有同学很容易就爆了，最后为了防止被认定为恶意hack，也是草草收场了。希望评测机以后能对多线程多测几次，感觉很多都是本地基本爆交上去一次都不爆的。。。



### 心得体会

终于，第二单元也正式落下帷幕了。如果不考虑失败的优化来说的话，其实于我而言，受到的挑战比第一单元稍小。一开始我的层次化设计就是围绕"存在即合理"这一核心设计的，生活中有什么就尽量做个大模拟去复现生活中所有的场景。在解决线程安全的时候也是，一边想现实生活中，系统是怎么维护运行而不出错的，一边阅读自己的代码逻辑一边修改代码。通过本单元的学习，我感受到了多线程的强大，能够提高CPU的利用率减少进程的运行时间，同时也提升了自己对于一开始代码如何架构的能力，相比于第一单元经历重构来说，这次作业没有经历大规模的重构，已经感到十分高兴了。虽然过程是困难的，但结果还是挺不错的。

但是也有不足的地方，在第二次作业早早就完成了随机版的情况下，没能写出来影子电梯，导致了性能分不高，第三次作业又因为许多事情堆在一起没时间再优化了，所以第二次和第三次作业的性能分都十分一般，没能探索到其他优秀的同学在写影子电梯时的优美设计。

在不足中成长，在探索中前行。这也许本身就是一种多线程吧(。