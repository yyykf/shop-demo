package cn.ykf.util;

/**
 * ID生成器，使用雪花算法
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/23
 */
public class IdUtils {
    /** 起始的时间戳 */
    private final static long START_STAMP = 1480166465631L;

    /** 数据中心占用的位数 */
    private final static long DATACENTER_BIT = 5;
    /** 序列号占用的位数 */
    private final static long SEQUENCE_BIT = 12;
    /** 机器标识占用的位数 */
    private final static long MACHINE_BIT = 5;


    /** 每一部分的最大值 */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    /** 每一部分向左的位移 */
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    private final static long MACHINE_LEFT = SEQUENCE_BIT;

    /** 数据中心 */
    private final long datacenterId;
    /** 机器标识 */
    private final long machineId;
    /** 序列号 */
    private long sequence = 0L;
    /** 上一次时间戳 */
    private long lastStamp = -1L;

    public IdUtils(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return id
     */
    public synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;

        // 时间戳部分
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                // 数据中心部分
                | datacenterId << DATACENTER_LEFT
                //机器标识部分
                | machineId << MACHINE_LEFT
                // 序列号部分
                | sequence;
    }

    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        IdUtils idUtils = new IdUtils(2, 3);
        for (int i = 0; i < 10; i++) {
            System.out.println(idUtils.nextId());
        }
    }
}
