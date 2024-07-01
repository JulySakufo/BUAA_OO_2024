# OO作业随笔记录-第一单元-表达式

## 第一次作业

### 递归下降的分析思路

- #### Expr 表达式

表达式 = 项 **+** 项 **+** 项构成，多个项组在一起就成了表达式，采用递归下降的方法也就是

表达式 = 项 **+** 表达式，对得到的表达式不断进行递归，设置递归条件就能得到完全的项展开。

单独的一个项也是表达式

- #### Term 项

项 = 因子 * 因子

**一个表达式，就是几个项而已。如果我们能够屏蔽项的解析过程，直接返回项的解析结果，那么就可以将表达式的解析过程化简为一次简单的下降。在高层次的代码编写中忽视局部问题的解决过程，假设已经被某个方法解决，直接调用方法即可。**

**ReplaceAll方法**

**BigInteger的使用，直接去掉前导0，使得存在容器的时候能够无脑使用String**

### 优化思路

- **coefficient=0,直接不打印，最后检查sb.toString是否为空，若为空直接打印0；**
- **coefficient=1,省略系数打印，只打印x^b^**
- **exponent=1，省略^与指数打印，只打印kx**
- **调整各项的位置，正项放前面，负项放后面**

### Hack数据

+-+1

x-x

****

## 第二次作业

新增需求：三角函数，自定义函数

三角函数可以新建一个类且需要`implements Factor`，(根据表达式的组成可知)

因子的共同属性？coe exp

**欧拉公式？建立三角函数与多项式之间的联系？**

`HashMap<String,BigInteger> hashmap`？ String存字符串指数，BigInteger存系数？

(e^2ix^ +e^-2ix^)/4   +    (e^2ix^-2+e^-2ix^)/4

<img src="C:\Users\28670\AppData\Roaming\Typora\typora-user-images\image-20240304151218966.png" alt="image-20240304151218966" style="zoom:50%;" />

```
(-1+x^233)*sin(x**2)
```

自定义函数

会不会先代入再化简会好处理一点？把自定义函数的调用当作预处理，直接替换parseExpr的expr，接下来按照第一次作业的expr的解析过程处理即可。

比如：

```
f(x)=x^2  (x+f(x))*x
```

先在给出的待化简表达式寻找f(x)，直接替换成为(x+x^2)*x，接下来就是对该表达式化简，从而模仿第一次作业。感觉这样处理好像挺不错的？利用前面已有的工具，逐步实现后续迭代过程的开发。





新增需求：指数函数、自定义函数

3\*5+5\*f(x)+2\*exp((x+1)\^2)\^2

通项:a\*x^b^*exp((Expr))^c^,

c=0,上一次作业所做的操作，只需再存储a,b

c!=0,需要存储的有a,b,Expr

`HashMap<String,Hashmap<String,HashMap<BigInteger,BigInteger>>>`

第一个String表示exp的指数是多少，第二个String表示指数函数里面的Expr是什么，剩下的就是coe和power

**需要处理的，Term类里识别到一个因子后该如何addFactor，Expr类识别到一个Term如何addTerm，统一操作是目的**

- **指数函数**`implements Factor`，也是因子的一环
  
  - 类似于幂函数，由`exp(<Factor>)`、指数符号`^`和指数组成，感觉就像幂函数base的x替换成了exp()?
  
  - `HashMap<String,ArrayList<HashMap<BigInteger,BigInteger>>>`的存储会不会有用？
  
    String表示是多项式因子()，还是指数函数因子因为多项式因子和指数函数因子是无法合并的
  
- 自定义函数`implements Factor`，因子的一环
  - 形参 x y z
  - 调用 Factor Factor Factor,是函数调用时的实参





### Hack数据

```
1
f(y,z,x)=-y^+02
++04831*f((-0000*f(-0001315,x,-001959)),f(-3171,x^+2,x),exp(x))
```

**测试function形参读入的顺序f(y,z,x)**



```
1
f(x)=x
f(x)^2
```

**测试对函数因子的解读**



```
1
f(x)=x
f((-x))

1
f(x,y)=x+y
f(-x,-x^2)
```

**测试自定义函数因子的带符号对不对**

```
1
f(x)=exp(4)
f(x)
```

**测试exp里面放常数对不对**
