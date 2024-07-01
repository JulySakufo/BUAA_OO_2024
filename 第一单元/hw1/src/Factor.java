import java.math.BigInteger;
import java.util.HashMap;

public interface Factor { //最基本的元素，作为底层。分为数字因子和表达式因子，因此number和expr都implements factor
    public HashMap<BigInteger, BigInteger> getHashMap();
    
    public void polyPow(int sum);
    
    public void negate();
}

