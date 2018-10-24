package org.tron.core.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tron.common.utils.ByteArray;
import org.tron.core.capsule.BlockCapsule;

@Slf4j
@Service
public class WitnessProductBlockService {

  private Cache<Long, BlockCapsule> historyBlockCapsuleCache = CacheBuilder.newBuilder()
      .initialCapacity(200).maximumSize(200).build();

  private Map<String, CheatWitnessInfo> cheatWitnessInfoMap = new HashMap<>();

  public static class CheatWitnessInfo {

    private AtomicInteger times = new AtomicInteger(0);
    private long latestBlockNum;
    private Set<BlockCapsule> blockCapsuleSet = new HashSet<>();

    public AtomicInteger getTimes() {
      return times;
    }

    public CheatWitnessInfo setTimes(AtomicInteger times) {
      this.times = times;
      return this;
    }

    public long getLatestBlockNum() {
      return latestBlockNum;
    }

    public CheatWitnessInfo setLatestBlockNum(long latestBlockNum) {
      this.latestBlockNum = latestBlockNum;
      return this;
    }

    public Set<BlockCapsule> getBlockCapsuleSet() {
      return blockCapsuleSet;
    }

    public CheatWitnessInfo setBlockCapsuleSet(
        Set<BlockCapsule> blockCapsuleSet) {
      this.blockCapsuleSet = blockCapsuleSet;
      return this;
    }

    @Override
    public String toString() {
      return "CheatWitnessInfo{" +
          "times=" + times.get() +
          ", latestBlockNum=" + latestBlockNum +
          ", blockCapsuleSet=" + blockCapsuleSet +
          '}';
    }
  }

  public void validWitnessProductTwoBlock(BlockCapsule block) {
    try {
      BlockCapsule blockCapsule = historyBlockCapsuleCache.getIfPresent(block.getNum());
      if (blockCapsule != null && Arrays.equals(blockCapsule.getWitnessAddress().toByteArray(),
          block.getWitnessAddress().toByteArray())) {
        String key = ByteArray.toHexString(block.getWitnessAddress().toByteArray());
        if (!cheatWitnessInfoMap.containsKey(key)) {
          CheatWitnessInfo cheatWitnessInfo = new CheatWitnessInfo();
          cheatWitnessInfoMap.put(key, cheatWitnessInfo);
        }
        cheatWitnessInfoMap.get(key).getTimes().incrementAndGet();
        cheatWitnessInfoMap.get(key).setLatestBlockNum(block.getNum());
        cheatWitnessInfoMap.get(key).getBlockCapsuleSet().clear();
        cheatWitnessInfoMap.get(key).getBlockCapsuleSet().add(block);
        cheatWitnessInfoMap.get(key).getBlockCapsuleSet().add(blockCapsule);
      } else {
        historyBlockCapsuleCache.put(block.getNum(), block);
      }
    } catch (Exception e) {
      logger.error("valid witness same time product two block fail! blockNum: {}, blockHash: {}",
          block.getNum(), block.getBlockId().toString(), e);
    }
  }

  public Map<String, CheatWitnessInfo> queryCheatWitnessInfo() {
    return cheatWitnessInfoMap;
  }
}
