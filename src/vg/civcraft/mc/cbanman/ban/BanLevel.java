package vg.civcraft.mc.cbanman.ban;

public enum BanLevel {
	HIGH (1),
	MEDIUM (2),
	LOW (3),
	TEMP (4),
	;
	private final byte level;

    private BanLevel(int level) {
        this.level = (byte)level;
    }
    
    public int getBanLevel(){
    	return (int)this.level;
    }
    
    public byte toByte(){
    	return this.level;
    }
    
    public BanLevel fromByte(Byte lv){
    	BanLevel level = null;
    	switch (lv){
    		case 1:
    			level = BanLevel.HIGH;
    			break;
    		case 2:
    			level = BanLevel.MEDIUM;
    			break;
    		case 3:
    			level = BanLevel.LOW;
    			break;
    		case 4:
    			level = BanLevel.TEMP;
    			break;
    	}
		return level;
    }
    
}
