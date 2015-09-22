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
    
    public BanLevel fromInt(int lv){
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
    
    public BanLevel fromByte(byte lv){
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
    
    @Override
    public String toString(){
    	switch (this.level){
			case 3:
				return "High";
			case 2:
				return "Medium";
			case 1:
				return "Low";
			case 0:
				return "Temp";
    	}
    	return null;
    }
    
}
