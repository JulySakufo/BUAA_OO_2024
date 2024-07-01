所找出的BUG现象为`Test delete`后并没有其他的输出结果，说明在`delete`的测试中是失败的
并且`Test insert`里面只进行了一次插入操作就退出了，说明`insert`操作也是
有问题的。综上说明问题出在`insert`函数和`delete`函数。

经过观察，发现只需要修改两处地方，`insert`函数将左边界的值改为0
`delete`函数将`while(left<right)`改为`while(left<=right)`即可改为正确
的二分插入与删除操作。