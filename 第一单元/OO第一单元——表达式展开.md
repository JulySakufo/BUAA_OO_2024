# OO第一单元—表达式展开

### 第一次作业

第一次作业中展开表达式的因子分为常数因子、幂函数因子、表达式因子三类。因为寒假的时候，oolens公众号推送了关于递归下降的文章，提前进行了阅读，所以说一开始就确定了递归下降解析表达式的思路，又因为第一次作业的训练题中的`advance`题提供了很好的`lexer`和`parser`架构，所以本次作业基本是在`advance`的基础上进行了增量开发。

#### UML类图

<img src="D:\OO\第一次作业UML.png"  />

#### 架构设计分析

##### 递归下降的分析思路

- **Expr 表达式**

表达式 = 项 **+** 项 **+** 项构成，多个项组在一起就成了表达式，采用递归下降的方法也就是

表达式 = 项 **+** 表达式，对得到的表达式不断进行递归，设置递归条件就能得到完全的项展开。

单独的一个项也是表达式

- **Term 项**

项 = 因子 * 因子

**一个表达式，就是几个项而已。如果我们能够屏蔽项的解析过程，直接返回项的解析结果，那么就可以将表达式的解析过程化简为一次简单的下降。在高层次的代码编写中忽视局部问题的解决过程，假设已经被某个方法解决，直接调用方法即可。**

##### 面向对象提供的解决思路

因为训练中的`advance`提供了`Factor`这个接口，`Expr`和`Number`都`implements`了这个接口，所以说可以通过在接口中声明抽象方法，到具体的类中实现以完成统一操作。面向对象的三大特点分别是封装，多态，继承。如果对于不同的对象，但是它们具有统一的属性和操作，我们能够不加区分地用统一的方法去实现目标，这是很好的选择。注意到表达式展开的最后的形式可以写成$\sum a*x^b$形式，也就是说事实上我们只需要记录a,b即可。在这次作业中我采用的是HashMap存储<exponent,coefficient>的键值对。

##### Parser

考虑到`+`，`-`对因子的影响，事实上`parser`的改动还是挺多的，在第一次作业中给我带来了挺大的困难。沿用递归下降的思路，依旧是`ParseExpr`，`ParseTerm`，`parseFactor`三种方法，递归解析，在`parseExpr`中使用`parseTerm`方法，在`parseTerm`中使用`parseFactor`方法。此处的关键是对负号的处理，我把它归结于项的属性，即遇到负号就实现`negate`方法给`Term`中存储的HashMap键值对的`coefficient`乘上`-1`，这样就实现了统一化的操作。

```java
public Expr parseExpr(){
    Expr expr = new Expr();
        if (lexer.peek().equals("-")) {
            lexer.next();
            expr.addTerm(parseTerm().negate());
        } else if (lexer.peek().equals("+")) {
            lexer.next();//读下一个
            expr.addTerm(parseTerm());
        } else {
            expr.addTerm(parseTerm());
        }
        
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) { //提取项的概念，使得在外部处理只有+
            if (lexer.peek().equals("-")) { //先识别符号
                lexer.next();
                expr.addTerm(parseTerm().negate()); //只管调用，不用管项的具体解析过程
            } else {
                lexer.next();
                expr.addTerm(parseTerm()); //只管调用，不用管项的具体解析过程
            }
            
        }
        return expr; //项的合并，成为最简式
}
public Term parseTerm(){ //项的解析，返回项的解析结果是一个项！
    //...
}
public Factor parseFactor(){ //最套路化的一集，按部就班即可,考虑所有可能出现到的因子形式就行
    //...
}
```

##### Term

为了利用好HashMap的特性，我采用了边解析边化简的方法，时时刻刻保持每一个Term的HashMap中的size为1，即存储的不是因子*因子，而是系数相乘，指数相加的最简形式。

```java
public class Term{
    private final HashMap<BigInteger, BigInteger> factors;
    public Term() {
        this.factors = new HashMap<>();
        factors.put(new BigInteger("0"), new BigInteger("1"));//做好相乘的准备
    } //<exponent,coefficient>
    
    public void addFactor(Factor factor) {
        //...
        for (BigInteger exponent1 : hashmap.keySet()) {
                BigInteger coefficient1 = hashmap.get(exponent1);
                BigInteger exponent2 = exponent.add(exponent1);
                BigInteger coefficient2 = coefficient.multiply(coefficient1);
            	resultMap.put(exponent2, coefficient2);//合并，保持最简形式
    }
}
```



##### Expr

模仿`Term`，在`addTerm`时也进行合并，不同于`Term`的是此时的合并方法是指数相等时候，系数才相加。

```java
public class Expr implements Factor {
    
    private final HashMap<BigInteger, BigInteger> variableMap; //存储着每个项的exp和coe值
    
    public Expr() {
        this.variableMap = new HashMap<>();
    }
    
    public void addTerm(Term term) {
        //...
        for (BigInteger exponent : hashMap.keySet()) {
            BigInteger coefficient = hashMap.get(exponent);
            if (variableMap.containsKey(exponent)) {
                variableMap.replace(exponent, coefficient.add(variableMap.get(exponent)));
            } else {
                variableMap.put(exponent, coefficient);
            }
        }
    }
}
```



##### Variable

此类是最底层，为了实现HashMap键值对，它也得有HashMap保证`addTerm`的实现。

```java
public class Variable implements Factor { //变量因子
    private final BigInteger coefficient;
    private final String name;
    private final BigInteger exponent;
    private final HashMap<BigInteger, BigInteger> hashmap = new HashMap<>();
    
    public Variable(BigInteger coefficient, String name, BigInteger exponent) {
        //...
    }
    public HashMap<BigInteger, BigInteger> getHashMap() {
        return this.hashmap;
    }
}
```

综上所述，代码通过统一化形式的操作基本实现了表达式展开的目标。



##### 优化思路

第一次作业的优化思路和实现还是挺简单的，在开始作业之前就已经想好了要优化的地方有哪些

- **coefficient=0,直接不打印，最后检查sb.toString是否为空，若为空直接打印0；**
- **coefficient=1,省略系数打印，只打印x^b^**
- **exponent=1，省略^与指数打印，只打印kx**
- **调整各项的位置，正项放前面，负项放后面**



##### 代码复杂度分析

采用了IDEA自带的Metrics工具进行了Metrics Calculation,得到的结果如下

<img src="C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240319103430770.png" alt="image-20240319103430770" style="zoom:50%;" />

ev(G)是用来衡量程序非结构化程度的，非结构成分降低了程序的质量，增加了代码的维护难度，使程序难于理解。基本复杂度高意味着非结构化程度高，难以模块化和维护。

iv(G)模块设计复杂度是用来衡量模块判定结构。软件模块设计复杂度高意味模块耦合度高，这将导致模块难于隔离、维护和复用。

v(G)用来衡量一个模块判定结构的复杂程度，数量上表现为独立路径的条数，即合理的预防错误所需测试的最少路径条数，圈复杂度大说明程序代码可能质量低且难于测试和维护。

- 因为我的`toString`方法和`Optimize`方法都是最后的输出函数，做了许多特判，拥有很多`if-else`结构，所以复杂度高，确实难以模块化和维护，一有变动整个代码的逻辑确实得做很大的修改。
- `parseFactor`方法也是落到了具体的分析上，与`lexer.peek()`息息相关，所以也有大量的`if-else`结构，因此结构化程度不是很好。
- 可以注意到`polyPow`的复杂度也挺高的，但是不同于其他的特判，这单纯是自己的实现方法没有完全的统一化，出现了不和谐，从而导致了代码行数的增加和写的丑陋，在第二次作业中通过重构，实现了该方法的优化。
- 注意到了$a*x^b$的基本形式，采用HashMap存储指数与系数的关系这是这次作业的优点。(同时它也为接下来的重构埋下了地雷......)



### 第二次作业

> 漫长的重构，我真的累了...

第二次作业新增了多层括号嵌套，指数函数因子以及自定义函数因子，难度提升很大。同时，因为第一次作业的HashMap具有特殊的处理性，导致无法在第一次作业的基础上进行迭代，我迫不得已地进行了快速的重构，采用了大多数人的`Poly-Mono`结构，不得不说，该结构确实具有天然的可迭代性。如果采用的是递归下降的思路，那么对于多重括号这一要求根本不需要考虑，当完成第一次作业的时候，它天然就是支持多层括号的。

#### UML类图

![](D:\OO\第一次作业UML.png)

#### 架构设计分析

##### Parser

即使是重构，`parser`，`lexer`的架构也是依旧可以沿用的，本次重构我没对`parser`做太大的修改，根据本次作业要求，新增了`parseFuncFactor`，`parseExpFactor`两种方法，并且对第一次作业中冗杂的`parseFactor`中对`Variable`，`Num`的解析方法做了提取，以使得`parseFactor`相当简洁明确。

```java
 public Factor parseFactor() { //因子的解析
        if (lexer.peek().equals("f") || lexer.peek().equals("g") || lexer.peek().equals("h")) {
            return parseFuncFactor();
        } else if (lexer.peek().matches("\\d+")) { //数字因子;
            return parseNumberFactor();
        } else if (lexer.peek().equals("exp")) { //指数函数因子;
            return parseExpFactor();
        } else if (lexer.peek().equals("(")) { //表达式因子
            return parseExprFactor();
        } else { //是幂函数因子
            return parseVariableFactor();
        }
    }
```

对`exp(<因子>)`的解析想了很久，一直在考虑到底是按形式化描述直接用`parseFactor`解析还是把因子看成表达式交给`parseExpr`递归下降分析，最后还是采用了统一当作表达式因子来看待，为了统一操作，对于自定义函数的因子读取也采用`parseExpr`进行了解析，方便化简表达式的统一形式操作。

```java
public Factor parseExpFactor() { //对指数函数的解析   exp(Factor)^?
        lexer.next();//跳过固有的左括号
        lexer.next();
        Factor factor = parseExpr();//用表达式方法解析
        lexer.next();//跳过右括号；
    	//...
    	return new Exp(factor,exponent);
    }

 public Factor parseFuncFactor() { // f(因子,因子,因子)
        final String name = lexer.peek(); //f|g|h
        lexer.next();//(
        lexer.next();
        ArrayList<Factor> realParameters = new ArrayList<>();
        Factor factor = parseExpr();
        realParameters.add(factor);
        while (lexer.peek().equals(",")) { //函数的调用未结束
            lexer.next();
            realParameters.add(parseExpr());
        }
        lexer.next();
        return new FuncFactor(name, realParameters);//函数因子返回
    }
```



##### Definer

因为高中数学老师反复强调先化简，后代值，但是在这里，我想了一下，发现在最后在展开f(x)函数是很不方便的，感觉了一下，觉得对于计算机而言，先代值后化简好像也没啥问题，因此采用了遇到输入时定义的函数就直接将实参替换掉形参，把抽象函数的f(x)直接转换成表达式，最后再化简表达式，即先解析后化简。

参考了往届学长的博客，我觉得这种设置成静态的方法是非常好并且非常简单方便的，所以我的作业也做了同样的处理。对于输入时的函数，因为函数名不重复，所以通过函数名建立两个HashMap用于输入待化简表达式时实参替换形参，以及生成替换后的表达式。

注意到`f(y,x)=x+y`，若采用`f(x,x^2)`直接替换的方式得到的结果会是$2*x^2$，所以在读入输入的自定义函数时要做一点小小的处理，即将形参中的x,y,z替换成\_x,\_y,\_z。同时注意到exp也含有x，因此先对exp进行替换成eap，如此便成功实现了输入的自定义函数的处理。

函数的调用也不复杂，建立形参与实参之间的对应关系，然后替换表达式就行了。

```java
public class Definer {
    private static HashMap<String, String> functionMap = new HashMap<>();//函数名获取形式化表达式
    private static HashMap<String, ArrayList<String>> parameterMap = new HashMap<>();//函数名获得形参列表
     public static void addFunction(String function) {
        //...
        if(string.charAt(i)==x,y,z,exp)
            do something//修改了形参读入顺序，防止是f(y,x,z)形式
        //...
        expr = expr.replaceAll("x", "_x");
        expr = expr.replaceAll("y", "_y");
        expr = expr.replaceAll("z", "_z");//防止f(y,x)=x+y,f(x,x^2)的异常
        expr = expr.replaceAll("eap", "exp");//防止exp的x被替换掉
        String name = String.valueOf(string.charAt(0));//f,g,h
        functionMap.put(name, expr);//函数名与表达式的对应
        parameterMap.put(name, virtualParameters);
    } //函数表达式的读入与形参化
}
```

****

以上的部分并不难，接下来的便是本次重构的重头戏部分，`Mono`和`Poly`之间的嵌套关系更是深深折磨我想了很多天...

##### Mono

在上一次作业的分析中提到，表达式化简的核心就是找到类似于数列通项的东西，可以称之为基本项，在本次作业中我注意到可以以$a*x^b*exp(Factor)^c$形式表示基本项，并且可以更简洁地把c放入exp中得到更加简洁的表达形式$a*x^b*exp(Expr)$形式，为了表示这个基本项的元素，因此建了`Mono`这个类，它表示这样一个基本单元。相应的，作为`Mono`的集合，新建了一个`Poly`类，它盛放的是众多`Mono`，管理`+`,`-`。`Mono`具有的`private`属性分别是`BigInteger`类型的`a`，`b`，以及`Poly`类型的exp内部盛放的东西(内部东西是什么不确定的，所以干脆用也具有复杂属性的`Poly`去表示)。

**Show me the code!**

```java
public class Mono { //最小单元a*x^b*exp(Factor)^c
    private BigInteger coefficient;//不作为输出，它的真实系数在poly的monomap里面
    private final BigInteger exponent;
    private Poly poly;
}
```

`Mono`作为表示的工具，真正的复杂运算不应该是通过`Mono`与`Mono`之间进行的，而是`Poly`与`Poly`之间进行所拥有的`Mono`间的运算。所以计算应该主要归于`Poly`的逻辑实现。

接下来考虑的是`Mono`间的合并问题。**能否合并？怎样合并？**上面说了`+`,`-`问题不归`Mono`管，因此考虑的是`Mono`之间的相乘。稍微想一下就知道大概是什么样的情况，系数相乘，指数相加。

```java
public void updateExpMap(Poly p) { 
        for (Mono mono : p.getMonoMap().keySet()) {
            poly.addMono(mono);
        }
    }
    
    public Mono mulMono(Mono mono) { //expMap具有private属性，不放入poly类中
        BigInteger coe = mono.getCoefficient();
        BigInteger power = mono.getExponent();
        Mono newMono = new Mono(coe.multiply(coefficient), power.add(exponent));
        newMono.updateExpMap(this.poly);
        newMono.updateExpMap(mono.poly);
        return newMono;
    }
```



##### Poly

采用`HashMap<Mono,BigInteger> monoMap`盛放mono，不能直接用`ArrayList<Mono>`盛放mono，不然在比较`Poly`是否相等的时候会出现比较严重的问题。除此之外，还有**深克隆**这个问题，也是狠狠给我来了一下。所以在写构造方法的时候就要写好构造方法，进行深克隆，防止出现你以为修改了没修改，你以为没修改但修改了的bug...我在这里是写了两个构造方法，一个用于基本的生成，一个用于深克隆。

```java
public class Poly {
    private final HashMap<Mono, BigInteger> monoMap; //mono与coe
     public Poly() {
        this.monoMap = new HashMap<>();
    }
    
    public Poly(HashMap<Mono, BigInteger> newMonoMap) {
        this.monoMap = new HashMap<>();
        for (Mono mono : newMonoMap.keySet()) {
            Mono newMono = new Mono(mono.getCoefficient(), mono.getExponent());
            Poly newPoly = new Poly(mono.getPoly().getMonoMap());
            newMono.setPoly(newPoly);
            monoMap.put(newMono, newMono.getCoefficient());
        }
    }
}
```

做好了这些预备工作，后续`Poly`的相加，`Poly`的相乘，`Poly`的次方运算就很自然了，不会再遭受浅克隆带来的一系列痛苦问题。稍微注意的是`Poly`如何相加十分重要，因为它决定了你的`Poly`的相乘，次方运算是不是能够复用，形式是否简洁的问题。我采取的依然是**朴素的思想——化整为零**。`Poly`的相加说到底还是一个一个的`Mono`相加而已，`Mono`能合并那么就改变系数，如果不能就放入`monoMap`中成为`Poly`的一个part。

```java
public Poly addPoly(Poly poly) { //poly的相加转化成mono的相加,mulPoly的基准
        Poly resultPoly = new Poly();
        for (Mono mono : this.monoMap.keySet()) {
            resultPoly.addMono(mono);
        }
        for (Mono mono : poly.monoMap.keySet()) {
            resultPoly.addMono(mono);
        }
        return resultPoly;
    }
```

好了，这下问题又回到了`Mono`身上，因为`Mono`和`Poly`相互调用的复杂关系，我也因此被绕晕了很久。同样的问题，**能不能合并？怎样合并？**明确合并条件是x的指数相等,exp里面装的东西一样，这就涉及到`equals`和`hashcode`方法的重写，通过阅读网上对`equals`和`hashcode`的介绍，能够轻易完成重写，此处就不再赘述。

```java
public void addMono(Mono mono) { //最小单元的添加方法
        if (monoMap.containsKey(mono)) { //有这个mono，改变系数，生成新Mono
            //...
            monoMap.put(newMono, coefficient);
        } else { //没有这个mono，放进去就行
            monoMap.put(mono, mono.getCoefficient());
        }
    }
```

完成了最最基本的底层操作，`mulPoly`，`powPoly`的实现就很简单了，从数学原理上去想相乘的过程，能够很容易就想明白要怎么写。



##### 方法的统一及最后的输出——toPoly

做完了上述工作，基本可以说解析的工作就完成了，接下来就需要完成最后的输出化简后的表达式的工作。利用相似性，可以明白一个项就是一个`Mono`，一个表达式就是`Poly`，同样`Mono`也可以看成`Poly`，这与一个单独的项可以看成表达式是一样的。因此同样采取递归下降的思路完成输出。

首先构建最基本的输出逻辑。`Expr->Term->Factor`。明确加减乘除关系，写出逻辑正确的代码，忽视细节，注意整体。到了最后的`factor.toPoly`，因为因子都统一接了`Factor`接口，所以在接口里面实现抽象方法`toPoly`，然后每个具体因子类具体实现即可。

如此，便很有面向对象的感觉了。

```java
//Expr.java
public Poly toPoly() {
        Poly poly = new Poly();
        for (Term term : terms) {
            do something...//考虑符号问题
            poly = poly.addPoly(term.toPoly());
        }
        return poly;
    }

//Term.java
public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(new BigInteger("1"), new BigInteger("0")));
        for (Factor factor : factors) {
            poly = poly.mulPoly(factor.toPoly());
        }
        return poly;
    }
```



#### 代码复杂度分析

![image-20240319202119249](C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240319202119249.png)

可以看见本次作业复杂度已经很低了，未截图出来的部分更低。此次作业在周六的时候因为没有考虑到深克隆的问题，所以在去除双层括号的时候遇到了很多问题，最后没有能在截止时间完成去除双层括号的优化版本，所以强测的性能分非常的烂，QAQ。



### 第三次作业

因为第二次作业的架构已经重构好了，拥有很高的可拓展性，所以第三次作业完成的非常快。第三次作业的新增内容是求导算子的引入，自定义函数的嵌套调用。事实上，在我的第二次作业中，就已经完成了自定义函数的嵌套调用，所以本次作业只需要完成求导算子的引入就行了。



#### UML类图

![](D:\OO\第三次作业UML.png)



#### 架构设计分析

求导算子也可以把它看作因子，在解析的时候遇到dx就进行`parseDeriFactor`的解析。通过SOLID原则，新增作为因子`DefiFactor`类，在`DeriFactor`里实现通用方法`toPoly`。

##### Parser

新增了求导因子的解析。

```java
public Factor parseFactor() { //因子的解析
        if(exer.peek().equals("dx")){
            return parseDeriFactor();
        }else{
            /*返回其他因子解析*/
        }
    }

public Factor parseDeriFactor() { //求导因子 dx(表达式)
        lexer.next();//左括号
        lexer.next();//因子
        Factor factor = parseExpr();
        lexer.next();//跳过右括号
        return new DeriFactor(factor);
    }
```



##### Poly

为了能够实现`DeriFactor`的`toPoly`方法，即对dx()操作进行运算。根据求导法则，对每个`Mono`求导，把最后的结果组合在一起就完成了求导操作。

```java
public Poly deriPoly() {
        Poly resultPoly = new Poly();
        for (Mono mono : monoMap.keySet()) {
            resultPoly = resultPoly.addPoly(mono.deriMono());
        }
        return resultPoly;
    }
```



##### Mono

对于一个`Mono`，它的因子是用`*`号连接起来的，故根据链式法则，前导后不导，后导前不导，得知对于一个`Mono`进行求导，得到的是Poly类型的。故根据逻辑即可写出代码

```java
 public Poly deriMono() { //a*x^b*exp(factor)求导
        //...
        resultPoly.addMono(newMono1);//对a*x^b求导
        Poly newPoly2 = this.poly.deriPoly();//对exp的内部进行求导
        resultPoly = resultPoly.addPoly(selfPoly.mulPoly(newPoly2));//exp求导乘以外部；
        return resultPoly;
    }
```

经过上述操作，便已经完成本次作业的基本要求了。



#### 优化思路

本次作业我完成了去除无脑双层括号的问题。通过判断exp()内部是否为因子，来决定是否添加括号。实际上就是看Poly是怎么样的。首先知道，如果Poly的`monoMap`内放的`mono`超过了一个，即`monoMap.size()>1`，那么一定不是因子，那么就只需要考虑`monoMap`的size为1的时候的情况。这又回归到`monoMap`装的`mono`本身。对于一个`mono`来说，怎样界定是否为因子呢？

- 只有单纯的系数
- 系数为1，且要么没有$x^b$的结构，要么没有$exp()$的结构
- 不符合上述两种情况的均不是因子

综上，就可以实现去除无脑双层括号了。为了保障正确性，我放弃了提取公因数的优化处理。



#### 代码复杂度分析

<img src="C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240320111755148.png" alt="image-20240320111755148" style="zoom:50%;" />

可以发现，此次作业并未对我的复杂度有什么影响，因为好的架构，本次作业完成的非常顺利，只增加了五十行代码便解决了问题。可以说是最轻松的一集了。



### 测试与hack

#### 测试

虽然我在第一单元编写代码的时候遇到了很多困难，但是还好，三次强测都全部通过了，互测也没有被别人成功hack。

自己做测试检测自己代码的时候主要借助的是同学的评测机。通过生成高cost的数据对代码进行高强度的检测，好几次都测出了通过中测代码的问题，然后对着数据进行debug并修改代码。当然，评测机的作用也是有限的。有几次我都是通过了评测机，结果发现自己无意捏的数据爆了自己的代码，然后又进行了针对性的修改。于我而言，评测机起到普适性的作用，从一个大范围评估代码的正确性，自己手搓数据起到定点爆破的作用，因为主观能动性，数据更加具有针对性，短短的数据起到的作用往往非常之大，比如第二次作业让许多人爆了的`exp((-x))`。

总之，通过评测机进行大数据的评测，加上自己手搓数据定点筛查，我顺利通过了第一单元测试。



#### hack

##### 第一次作业

这次作业的hack只成功了两次，是发现同学的常数用的是`int`类型的，而并非`BigInteger`类型。

测试数据为：10000000000000000000000

属于是随便想的，因为讨论区有同学问指数类型，所以想了想会不会有同学还是用的`int`，所以hack成功。

##### 第二次作业

本次作业难度加大，我以为本次作业能够hack挺多的()，但是也只hack成功了一次。

测试数据为:

```
3
f(z,y,x)=-+exp(z) 
g(x)=++005+-0-(+x^+8)^1
h(z,x)=-+z	
+x*exp((--x^ +4*exp( 5423333333 ) )^+2)
```

hack成功是因为该同学的答案对于exp里面不是因子的时候少了括号。

##### 第三次作业

> 哎，还可以测TLE！ --我的室友

这次hack是最爽的一集。一共刀了10刀且高达20%的命中率，一刀3爆，一刀4爆，均是因为同学卡TLE了。一开始因为本次作业的代码增加量较少，加上大家都已经修复了第二次作业带来的bug，我以为这次会颗粒无收。但是一切的起源都源于室友成功hack了一次TLE。于是我也开始尝试构造这样的数据。另外一个室友发现了exp的cost代价非常小，exp的嵌套也只是+1罢了，而单纯的exp基本上并不会让人爆掉，但是考虑到函数因子的替换，我们将多层exp放进函数里面，并且函数嵌套函数，室友最开始造出来的数据是

```
2
g(z)=exp(exp(exp(exp(z))))
f(y)=exp(exp(exp(exp(g(y)))))
f(x)*g(x)-f(x)*f(exp(exp(exp(exp(x^8)))))
```

hack成功的其实是后面的f(exp(exp(exp(exp(x^8)))))，看起来以为cost会很大，其实很小，因子的cost也才刚刚12。室友用这个数据在他的房间里刀掉了4个人，抱着试一试的态度，我将这个数据放入了手动的评测机，发现我的房间也有三个人跑这个数据需要的时间相当之久。这就hack成功了三个人。

此外，因为用dpo oj的时候总有一个人跑数据的时候会出现re，我觉得十分奇怪，并且用该组数据跑同学的jar文件非常卡顿，但是这个数据又没有让他TLE掉。于是我开始了不停的尝试。想到函数的嵌套会更加的复杂，我将上述数据改造成

```
2
g(z)=exp(exp(exp(exp(z))))
f(y)=exp(exp(exp(exp(g(y)))))
f(g(exp(exp(exp(exp(x^8))))))
```

即在上面的核心hack数据外又套了一层函数，这下发现那位同学TLE了。我将这个数据分享给我的室友，发现之前从未被hack成功过的同学也因为这个数据TLE了，lol。他在一阵慌乱之后开始想自己要是被hack了该怎么修。他使用`JProfile`进行测时，通过测时，他找到了这个数据花时最多的时间是在toString上，46s的总时长中有45s都在进行toString的调用。值得一提的是，这个数据就算外层再嵌套一两层函数依然是在cost里面的，这个数据在我们寝室经过测试可以在四个房间内刀掉十几个人左右，感觉可以说是神级数据了，lol

<img src="C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240320164546229.png" alt="image-20240320164546229" style="zoom:50%;" />

上面这个图片是TLE同学的，对他的代码我也采用了`JProfile`的分析，得到了同样超时的效果。他们都是在每次在调用toString方法的时候优化一遍，这导致了TLE。

综上，我认为hack的思路如下

- 评测机找错并缩减结构。

评测机很多时候的cost是很大的，我在debug的时候经常就是通过拆分结构运行，找到自己发生bug的最小分支，并通过最小分支逐步调试，从而成功完成修改。在对同学进行hack的时候也可以采取类似的思路

- 手动构造针对性强的数据

利用边界条件，比如这次作业的exp多层嵌套却具有cost小的特性，把输入的数据的cost发挥到极致，大概率就能hack成功。

在hack的时候其实并没有去看同学的代码，因为代码结构太复杂且每个人具体的思路不一样，阅读代码感觉具有相当的复杂度，我主要就采取以上两种方法完成个人的hack。

### 心得体会

兜兜转转，这一单元总算落下帷幕了。第一周第二周作业没找到正确的思路和方向前基本天天都是痛苦的，随时随地都在写OO，因为之前写的语言都是C的，从面向过程到面向对象不是十分适应。但是写到第三次作业的时候好像有一点开悟了，一个任务的完成无非就是几个对象各做各的事，最后合起来任务就成功了(有点像小组作业)。有时候写着写着突然发现好像就能成功运行了，这是之前面向过程没发现过的。最后，一定要注意代码的扩展性，考虑好未来扩展需求，为需求留下空间，选择合适的架构，这太重要了。

### 未来方向

感觉可以平衡一下第二三次作业的难度，第三次作业半个小时就写完了，第二次作业写了好久好久...
