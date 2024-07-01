public class DeriFactor implements Factor {
    private final Factor factor;
    
    public DeriFactor(Factor factor) {
        this.factor = factor;
    }
    
    @Override
    public Poly toPoly() {
        return factor.toPoly().deriPoly();
    }
}
