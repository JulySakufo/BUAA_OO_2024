# OO第三单元——JML规格

### 本单元的测试过程

- #### 白箱测试

白箱测试是在已知源代码的编写情况下对代码进行测试的。测试的时候采用人工构造`special data`，已知代码各种分支的情况下，为保证能够全面运行，而手捏的数据。并自己给出正确的期待值。

- #### 黑箱测试

黑箱测试是未知代码的具体编写情况下对程序进行测试的。测试的时候大多数是随机生成数据，通过程序已有的输入输出接口对程序进行测试。因为是随机生成数据，所以基本都是拥有一个数据生成器，确保大范围的数据覆盖度，保证测试的准确性。

- #### 单元测试

本单元作业中，我们每一次作业中所做的就是单元测试。单元测试是针对程序中的最小可测试单元进行的测试，一般来说，是对方法进行测试。它实际上也是白箱测试，因为我们默认是知道源代码的，通过测试我们检查代码的逻辑和输出是否正确。

- #### 功能测试

功能测试只保证我们测试的单元能够正常实现功能，而不关心功能与功能之间的影响。那不是功能测试的事情。只要正确地实现了功能，我们就认为功能测试是成功的。

- #### 集成测试

对代码整体的测试，功能测试提到功能测试只关注功能本身能否正确实现，而不关注功能与功能之间的影响。该测试就是用于检查功能与功能之间的联系和影响，必须要正确实现功能与功能之间的关系才能通过测试。

- #### 压力测试

根据数据范围构造极端数据进行测试，比如测试`TLE`、`MLE`的情况，在本单元作业中的压力测试为`TLE`，通过反复使用规格中复杂度高的方法，检查是否`TLE`，从而将数据构造成最后一直使用某条指令，具有十足的特殊性，这就是压力测试。它也是我在hack时候经常采用的方法。

- #### 回归测试

因为工程上一般是迭代开发的，所以有些时候难免保证实现了新的功能后原功能是否受到影响。所以迭代开发后要重新进行测试，保证新写的代码没有改变原有的代码功能。

- #### 数据构造有何策略

我的想法非常简单而自然。通过随机数据生成器，大范围的生成一般性数据，充分覆盖测试范围，通过大规模的随机数据测试正确性和普适性。然后根据`JML`规格找出复杂度最高的方法，反复使用该方法，执行压力测试，测试程序在极端数据下的表现情况和正确程度。我认为通过一般性和特殊性两种性质的方法测试，基本都能保证代码的正确性。



### 本单元的架构设计

- #### 第一次作业

第一次作业的主要难点方法在`isCircle`方法上。通过学习，我知道了并查集对于连通性的判断非常简单而高效。因此对于连通性的判断，我引入了并查集，用来实时记录图的连通情况。

连通集具有两种特征性的操作，正如英文名所言`union-find-set`，它的代表性操作也正是`union`与`find`。为了降低复杂度，我又采用了路径压缩和按秩合并两种优化方式。

##### 路径压缩

在执行`find`操作的时候，顺带更新查找过程中每个结点的根节点，是采用递归的方式，不仅逻辑简单实现起来也相当优雅。

```java
public int find(int x) {
        if (rootMap.get(x) == x) {
            return x;
        }
        rootMap.put(x, find(rootMap.get(x)));
        return rootMap.get(x);
    }
```

##### 按秩合并

为了使生成的树的高度与之前相比尽量小，而不是出现一边的高度很高一边高度低，即为了平衡树的高度而采用了按秩合并的优化方法。

```java
public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            blockSum--;//两个孤岛联合成一个了,块的数量自然减少
            if (rankMap.get(rootX) > rankMap.get(rootY)) {
                rootMap.put(rootY, rootX);
            } else {
                if (rankMap.get(rootX).equals(rankMap.get(rootY))) {
                    rankMap.put(rootY, rankMap.get(rootY) + 1); //高度相等升秩
                }
                rootMap.put(rootX, rootY);
            }
        }
    }
```

这样实现过后，通过在`addPerson`增加一个孤立结点，在`addRelation`和`modifyRelation`维护并查集，就能实时完成`network`中连通性的更新。这样一来，实现`isCircle`的方式也就很简单了，仅仅需要一行`return find(id1) == find(id2)`就能解决。换句话说，我们在并查集中已经实现了凡是在一个连通图中的结点，所有结点的根节点都是相同的。比较两个结点是否在同一个连通图，也就是比较两个结点的根节点是否是同一个。

此外有稍稍注意的情况就是，在`modifyRelation`的时候可能会删除掉两个结点之间邻接的边，这会改变图的连通情况。为了正确实现连通情况，我们需要在删除边的时候同时维护并查集。我采取了重建两个结点的并查集的关系来实现我的目的。具体而言，假如我们要删除1，2之间的边，先通过`dfs`，找到所有与1具有关系的点，如果其中还有2这个结点，那么可以认为1，2之间的连通性是没有改变的，这种情况下只需要将所有结点的根节点指向1完成更新即可。如果没有2这个结点，那么就需要将与1有关系的点的根节点指向1，与2有关系的点指向2，同时完成`rootMap`和`rankMap`的更新就完成`modifyRelation`时候的并查集的维护了。

还有两个规格上复杂度较高的方法是`queryBlockSum`和`queryTripleSum`，对于这两个方法，我采用的都是动态维护。`blockSum`的维护很简单，新增结点的时候就`blockSum++`，执行`union`操作的时候就`blockSum--`，这样就实现了操作总复杂度是`O(n)`，对于查询这一操作而言操作的时间复杂度就是`O(1)`。较于`JML`规格而言，显著降低了时间复杂度。`TripleSum`关系的维护也很简单，根据常识，我们知道是否形成了三元环只需要在`addRelation`和`modifyRelation`进行维护即可。我采用的是如下代码。

```java
tripleSum += myPerson1.getDegree() < myPerson2.getDegree() ?
                    myPerson1.getTripleSum(myPerson2) : myPerson2.getTripleSum(myPerson1);

tripleSum -= myPerson1.getDegree() < myPerson2.getDegree() ?
                        myPerson1.getTripleSum(myPerson2) : myPerson2.getTripleSum(myPerson1);
```

同时做了个小的优化，选择度小的去遍历，这也使得操作复杂度是O(n)，并且每次的操作复杂度也减小了。



- #### 第二次作业

第二次作业的重心在于`queryBestAcquaintance`、`queryCoupleSum`、`queryShortestPath`上。先说说`queryShortestPath`，经过翻译，我们知道它想做到的意思就是求出一个点到另一个结点经过的最短的距离。最短，我们可以用`BFS`，经过查询资料和同学分享，我采用了双向`BFS`的方法。

<img src="C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240516105828268.png" alt="image-20240516105828268" style="zoom:50%;" />

如上图所示，它显著降低了搜索的时间和空间复杂度。双向`BFS`的实现很简单，是在一般的`BFS`从起点一直搜索到终点，变成了起点终点同时开始搜索，搜索到相同的结点就停止搜索，说明已经可以构建一条从起点到终点的路径。写法跟一般的`BFS`区别不大，思路上挺简单。其次是`queryBestAcquaintance`，我同样采用了动态维护的方法。在每个`MyPerson`中新增了当前的`bestId`和`bestAcquaintance`，如果有新的关系构建，就比较两者的`id`和`value`大小，实现更新。如果修改了关系根据具体情况选择是否遍历更新`bestAcquaintance`，它们的复杂度都是O(n)。

最后是`queryCoupleSum`，我实现的方法也是O(n)，并且考虑到有没修改关系却一直在查询的情况不需要重新构建，设置了`coupleDirty`脏位，如果修改了关系`coupleDirty`为`true`,那么就需要重新计算`coupleSum`，否则直接返回已经计算好了的`coupleSum`即可。

```java
public int queryCoupleSum() { //要么重建，要么直接返回.o(n),o(1)
        if (!coupleDirty) {
            return coupleSum;
        }
        coupleSum = 0;
        HashSet<MyPerson> visited = new HashSet<>();
        for (MyPerson myPerson : peopleMap.values()) {
            if (!visited.contains(myPerson)) {
                HashMap<Integer, MyPerson> acquaintance = myPerson.getAcquaintance();
                if (acquaintance.isEmpty()) { //还没有诞生best属性
                    continue;
                } //1有2，2必有1，因此不必判断bestPerson的acquaintance是否为空了
                MyPerson bestPerson = acquaintance.get(myPerson.getBestAcquaintance());
                if (bestPerson.getBestAcquaintance() == myPerson.getId()) {
                    coupleSum++;
                    visited.add(myPerson);
                    visited.add(bestPerson);
                } else {
                    visited.add(myPerson);
                }
            }
        } //重建完成
        coupleDirty = false;
        return coupleSum;
    }
```

最后是很多人出错的`queryTagValueSum`，相比于我身边同学的O(mn)的复杂度，我采取了O(n)的复杂度的实现，即实现一个`static`的`tagMap`，数据类型为`HashMap<Integer,HashSet<MyTag>>`，即记录该id的人所在的tag。在对tag进行操作的时候实时维护`tagMap`，就可以做到以很小的时间复杂度维护`valueSum`了。考虑到木桶效应，一开始我认为同学的遍历所有人的所有`tag`会超时，因为整个程序的最坏复杂度可以做到O(n^2)，结果互测数据的数据条数使这种想法不能实现。但是在本地的性能测试中，对于很多组随机数据，我的程序都显著跑的很快。维护`valueSum`，在我的实现中很简单。比如改变了1，2之间的关系，只需要找到1，2共同的`tag`即可。即通过遍历一次`hashset`即可找出公共的`tag`，`updateValueSum`即可。其中`updateValueSum`也做了小小的优化，找到`hashset.size`小的那个，遍历那个`hashset`然后执行`Mytag.updateValueSum`

```java
public void updateValueSum(int id1, int id2, int value) {
        if (tagMap.containsKey(id1) && tagMap.containsKey(id2)) { //首先都要保证被加入到了tag
            HashSet<MyTag> hashSet1 = tagMap.get(id1);
            HashSet<MyTag> hashSet2 = tagMap.get(id2);
            HashSet<MyTag> hashSet = hashSet1.size() < hashSet2.size() ? hashSet1 : hashSet2;
            int yourId = hashSet1.size() < hashSet2.size() ? id2 : id1;
            for (MyTag myTag : hashSet) { //做优化，遍历数量少的tag
                if (myTag.hasPerson(getPerson(yourId))) { //2个人在同一个tag，更新value
                    myTag.updateValueSum(value);
                }
            }
        }
    }
```



- #### 第三次作业

本次作业是三次作业中最简单的一次，`JML`规格给出的方法都是O(n)的，照着翻译即可。没有什么特殊化的处理也没有动态维护。



### 规格与实现分离

参考了往年博客，我知道了完全按照`JML`规格翻译是行不通的。换句话说，`JML`规格只保障了正确性，而没有考虑性能。提供的数据类型并不是我们要实现的，我们可以使用更好的数据类型完成实现。

在我的代码中，我采用了两种方法来理解规格和翻译规格。

- 直接翻译规格

```java
 /*@ public normal_behavior
      @ requires obj != null && obj instanceof Person;
      @ assignable \nothing;
      @ ensures \result == (((Person) obj).getId() == id);
      @ also
      @ public normal_behavior
      @ requires obj == null || !(obj instanceof Person);
      @ assignable \nothing;
      @ ensures \result == false;
      @*/
    public /*@ pure @*/ boolean equals(Object obj){
        if (obj == null || !(obj instanceof MyPerson)) {
            return false;
        }
        return ((MyPerson) obj).getId() == this.id;
    }
```

这种简单操作直接翻译规格即可。

- 理解规格，采用性能更高的方法

```java
//@ ensures \result == (\exists int i; 0 <= i && i < tags.length; tags[i].getId() == id);
    public /*@ pure @*/ boolean containsTag(int id){
        return tags.containsKey(id);
    }
```

`JML`规格中给出的`tags`是一个数组，对于查询没有好的方法， 只能遍历数组。这对于多查询的操作是极为糟糕的。因此理解了该方法想要表达的查询意思后，我采取了查询操作最快的`hashmap`，查询的时间复杂度为O(1)，这就是规格与实现相分离的好体现。

三次作业中明确了这一点的我在强测和互测中都没有出bug，性能也尚可，毕竟都是O(n)的操作。



### Junit测试

通过学习实验课的`parameters`方法，很容易就学会在junit中如何随机生成数据。`test`方法的编写也很简单，通过`JML`规格翻译得到一个结果，再运行一遍自己编写的方法得到一个结果，通过`assertEquals`比较两个结果就可以知道自己写的是正确的还是错的。效果也还不错，我的室友甚至在编写junit后测出了自己程序的bug。

如何利用规格信息更好的设计实现junit测试呢？

要注意到方法的一系列限定词。

- **pure**

该方法要求调用方法前后，容器内的数据不产生任何变化。如果产生了变化，即使返回值是正确的，也视为不正确。对于基本数据类型可以用`==`进行值的验证，对于非基本数据类型需要用`equals`方法判断是否相等。

- **ensures**

逐一验证后置条件是否正确。

- **assignable**

如果不是`nothing`，需要验证除了`assignable`的实例变量之外其他变量在调用方法前后不变。

把握住以上限定词，明确通过`JML`实现单元测试就是`JML`直译一遍，自己写的优化方法跑一遍，两相对比，很容易就写完junit测试了。



### 本单元学习体会

经历过前两单元的学习，本单元的强度降低不少，给了人缓冲的机会。有点梦回oopre的感觉。虽然jml的运用很少，但是自己上手编写过才发现jml编写起来相当复杂，用jml表述而不使用自然语言表述，就是因为自然语言描述不同人理解起来意思也许会不同。而这种表述方法，事实就是事实，理解了大致的变化，也就能理解目的到底是什么。