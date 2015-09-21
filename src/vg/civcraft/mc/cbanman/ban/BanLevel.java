package vg.civcraft.mc.cbanman.ban;

public enum BanLevel {
	HIGH (3),
	MEDIUM (2),
	LOW (1),
	TEMP (0),
	;
	private final byte level;

    private BanLevel(int level) {
        this.level = (byte)level;
    }
    
    public int getBanLevel(){
    	return (int)this.level;
    }
    
    public byte value(){
    	return this.level;
    }
    
    public BanLevel fromByte(Byte lv){
    	BanLevel level = null;
    	switch (lv){
    		case 3:
    			level = BanLevel.HIGH;
    			break;
    		case 2:
    			level = BanLevel.MEDIUM;
    			break;
    		case 1:
    			level = BanLevel.LOW;
    			break;
    		case 0:
    			level = BanLevel.TEMP;
    			break;
    	}
		return level;
    }
    
}
