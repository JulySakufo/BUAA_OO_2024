# JML学习

### JML表达式

- `\result`表示方法的返回值，比如`\result == id`表示方法的返回值为id

- `\old(expr)`表示expr在这个方法执行前的取值
- `\forall`全称量词，对给定范围的元素，每个元素都满足对应的约束
- `\exists`存在量词，存在
- `\sum`，求给定表达式的和。`(\sum int i; 0<=i && i<5 ; i)`求[0,5)的整数的和
- `\product`求给定表达式连乘的结果
- `\max`求给定表达式中的最大值
- `\min`求给定表达式中的最小值
- `\num_of`返回指定变量中满足相应条件的取值个数，如`(\num_of int x; 0<x&&x<=20;x%2==0)`得到的结果就为10，一般形式可以写为`(\num_of Type x;R(x);P(x))`
- 等价关系`b_expr1<==>b_expr2`要求`b_expr1`与`b_expr2`都是布尔表达式。就是表示一种等价关系。
- 推理关系`b_expr1==>b_expr2`与离散数学的蕴含一样。当`b_expr1==false`或者`b_expr1==true&&b_expr2==true`整个表达式为`true`
- `assignable`，`assignable \nothing`表示每个变量都不能在方法执行过程中被赋值。`assignable \everything`表示每个变量都可以在方法执行过程中被赋值。
- `pure`，无需前置条件，不会有副作用，执行一定会正常结束。多是`get`方法

```java
public /*@ pure @*/ String getName();

//@ ensures \result == bachelor || \result == master;
public /*@ pure @*/ int getStatus();

//@ensures \result >= 0;
public /*@ pure @*/ int getCredits();
```

- `public normal_behavior`表示正常功能行为，`public exceptional_behavior`表示异常行为，如果一个方法是不`throws exception`的话就没必要用上面两种情况修饰。

### 方法规格

- `requires`，`requires P`意思是调用者保证P为true。(`requires P1||P2`)表示需要P1||P2为true。依我看来，如果一个方法没有抛出异常的话，相当于给出了数据限制。即这个方法调用的时候要满足数据限制才可以被调用。
- `assignable`，既可默认为当前作用域所有类成员变量和方法输入对象都可以赋值或修改，也可以指具体可修改的变量列表，如果是多个用逗号分隔，如`@assignable elements,max,min`
- `ensures`，`ensures P`表示返回结果确保P为true
- 如果是`public exceptional_behavior`，`ensures`改为`signals`。`signals (IllegalArgumentException e) b_expr`表示`b_expr`为`true`时，抛出异常e。异常可自定义。如果一个方法在运行时会抛出异常，一定要在方法声明中加上`throws ...`，...代表具体的异常类型。多个异常用逗号分隔。如`public int record(int z) throws IllegalArgumentException, OverFlowException{...}`
- `invariant`不变式，在任意状态下都是不变的。`constraint`类似。
- `spec_public`注释一个类的`private`成员，表示可以在jml规格中直接使用，调用者可见。