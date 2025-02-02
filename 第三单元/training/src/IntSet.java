public interface IntSet {
    //@ public instance model int[] ia;
    //@ public invariant (\ int i, j; 0 <= i && i < j && j < ia.length; ia[i] < ia[j]);
    
    
    //@ ensures \result == (\exists int i; 0 <= i && i < ia.length; ia[i] == x);
    public /*@ pure @*/ Boolean contains(int x);
    
    /*@ public normal_behavior
      @ assignable \nothing
      @ requires 0 <= x && x < ia.length;
      @ ensures \result == ia[x];
      @ also
      @ public exceptional_behavior
      @ signals (IndexOutOfBoundsException e) (x < 0 || x >= ia.length);
      @*/
    public int getNum(int x) throws IndexOutOfBoundsException;
    
    /*@ public normal_behavior
      @ assignable ia;
      @ requires !contains(x);
      @ ensures contains(x);
      @ ensures (\forall int i; 0 <= i && i < \old(ia.length); contains(\old(ia[i])));
      @ ensures (\forall int i; 0 <= i && i < ia.length && ia[i] != x; \old(contains(ia[i])));
      @*/
    public void insert(int x);
    
    /*@ public normal_behavior
      @ assignable ia;
      @ requires contains(x);
      @ ensures !contains(x);
      @ ensures (\forall int i; 0 <= i && i < ia.length; \old(contains(ia[i])));
      @ ensures (\forall int i; 0 <= i && i < \old(ia.length) && \old(ia[i]) != x; contains(\old(ia[i])));
      @*/
    public void delete(int x);
    
    //@ ensures \result == ia.length;
    public /*@ pure @*/ int size();
    
    /* 该方法完成两个IntSet对象所包含元素的交换, 例如：
       IntSet对象a中的元素为{1，2，3}，IntSet对象b中的元素为{4，5，6}
       经过交换操作后，a中的元素应为{4, 5, 6}, b中的元素为{1, 2, 3}
       两个IntSet对象中元素的数量可以不相同，例如：
       IntSet对象a中的元素为{1，2，3，4}，IntSet对象b中的元素为{5，6}
       经过交换操作后，a中的元素应为{5, 6}, b中的元素为{1, 2, 3，4}
       该方法无返回值
      */
    //TODO
    /*@ public normal_behavior
      @ assignable this.ia, a.ia;
      @ ensures (\forall int i; 0 <= i && i < \old(this.ia.length); this.contains(\old(this.ia[i])));
      @ ensures (\forall int i; 0 <= i && i < \old(a.ia.length); a.contains(\old(a.ia[i])));
      @ ensures (\forall int i; 0 <= i && i < a.ia.length; a.ia[i] == \old(this.ia[i]));
      @ ensures (\forall int i; 0 <= i && i < \old(this.ia.length); \old(this.contains(\old(this.ia[i]))));
      @ ensures (\forall int i; 0 <= i && i < \old(a.ia.length); \old(a.contains(\old(a.ia[i]))));
      @*/
    public void elementSwap(IntSet a);
    
    /* 该方法返回两个IntSet对象的对称差运算结果
       数学上，两个集合的对称差是只属于其中一个集合，而不属于另一个集合的元素组成的集合
       集合论中的这个运算相当于布尔逻辑中的异或运算，如 A,B两个集合的对称差记为A⊕B，
       则 A⊕B = (A-B)∪(B-A) = (A∪B)-(A∩B)
       例如：集合{1,2,3}和{3,4}的对称差为{1,2,4}
       如果对空的IntSet对象进行对称差的运算，将抛出NullPointerException的异常
      */
    //TODO
    /*@ public normal_behavior
      @ requires a != null;
      @ assignable \nothing;
      @ ensures (\forall int i; 0 <= i && i < \result.size(); \result.contains(this.ia[i]) || a.contains(this.ia[i]));
      @ ensures (\forall int i; 0 <= i && i < a.size(); \result.contains(a.ia[i]) || this.contains(a.ia[i]));
      @ ensures (\forall int i; 0 <= i && i < this.size(); !\result.contains(this.ia[i]) || a.contains(this.ia[i]));
      @ ensures (\forall int i; 0 <= i && i < a.size(); !\result.contains(a.ia[i]) || this.contains(a.ia[i]));
      @ ensures (\forall int i, j; 0 <= i && i < j && j < \result.size(); \result.ia[i] < \result.ia[j]);
      @*/
    public /*@ pure @*/ IntSet symmetricDifference(IntSet a) throws NullPointerException;
    
    //@ ensures \result == (\forall int i, j ; 0 <= i && i < j && j < ia.length; ia[i] != ia[j] && ia[i] < ia[j]);
    public /*@ pure @*/ boolean repOK();
}
