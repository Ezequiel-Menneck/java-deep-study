- G1 and ZGC: https://www.alibabacloud.com/blog/a-detailed-explanation-of-jvm-garbage-collector-g1%26zgc_601536

- Shenandoah: https://developers.redhat.com/articles/2024/05/28/beginners-guide-shenandoah-garbage-collector#

**Garbage collection in Java** is an automatic memory management process that hepls Java programs to run efficiently. Javan programs compile to bytecode that can be run on a *Java Virtual Machine(JVM).* When Java programs run on the JVM some objects are allocated in heap, which is a portion of memory dedicated to the program. Eventually, some objects will no longe be needed so the **Garbage collector** find these unused objects and deletes them to free up memory.

## G1 (Garbage First) Garbage Collector

Was developed in JDK7 and its functionality was fully implemented in JDK8. It successfully replaced Parallel Scavenge and became the default GC in server mode. Compared with another garbage collector CMS, G1 can not only provide regular memory but also achive predictable pauses and control garbage collection time within N milliseconds.

G1 collects both the young generation and the old generation at the same time, but these are referred to as G1’s Young GC mode and Mixed GC mode. This feature comes from G1’s unique memory layout. G1 tracks each Region of memory and maintains in a priority list of Regions. It selects an appropriate region at the right time for collecting. This Region-base provides the basis for some remarkable design ideas to address pause time and high throughput.
### Region Memory Division

G1 divides the heap into a series of memory regions of varying sizes, called Regions. Each region is 1-32M, which is the nth power of 2. Regions can logically divided into Eden, Survivor and the Old Generation. Each region can be Eden, Survivor or Old region, but at given any time it can only be one type of region. The number of regions of each role is not fixed, which means that the memory for each generation is also not fixed.

Also these three regions we have a special region in G1, the Humongous region. Humongous is used to storage large objects. When the capacity of an object exceeds half of the region size the object will be put into Humongous region, because if the replication algorithm is used to collect a large object that exists for a short period of time, the replication cost will be very high. Directly putting objects into the Old region causes objects that should only exists for a short time to occupy memory in Old Generation, which makes GC performance go down. If the object size exceeds the size of a Region you need to find a contiguous Humongous region to store this object.

![[image.png]]

  

## Region Internal Structure  
### Card Table
Is the internal structure of a Region. Each region is divides into a several memory blocks, called cards. These card set are called card tables.

In te following example the memory area in region1 is divided into 9 cards, and the set of 9 cards is the card table
![[image 1.png]]
### Remember Set
Each region contains a Remember Set or RSet. RSet is a hash table. The key is the staring address of other regions that reference the current region. The value is the index position of the card that is referenced by the region corresponding to the key in the current region.

RSet is used to solve “cross-generation reference”. In Young GC, when RSet exists, it follows the reference chain to search for references. If an old generation ob ject appears on the reference chain, then simply abandon the search for the reference chain.
### Young GC Process
1. **Stop the World (STW):** The entire Young GC process is performed within an STW pause, which is why Young GC can reclaim all Eden regions. The only way to control the overhead of Young GC are to reduce the number of Young regions, which means reducing the size of the young generation memory, and to increase concurrency by having multiple threads perform GC simultaneously to minimize STW time.

2. **Scan GC Roots:** Here the scanned GC Roots are those in the general sense, meaning they directly point to objects in the young generation. If a GC Root directly points to an object in the old generation, the scan will stop at this step and not proceed further.

3. **Drain the Dirty Card Queue and update the RSet:** The RSet records which objects are referenced across generations by old generation, when young generation objects is referenced by the old generation object, this record should be updated to the RSet. However this update dont occurs immediately. Whenever the old generation references a young generation object, the card address of this reference is actually put into the Dirty Card Queue (which is a thread private; when this thread is full it will be transferred to the global Dirty Card Queue, which is unique). The capacity of the global Dirty Card Queue changes is 4 phases.

  

![[image 2.png]]

**White:** Nothing happengs.

**Green**: Refinement threads are activated, with the number of threads specified by -XX:G1ConcRefinementGreenZone=N. A dirty card is taken from the global and thread-private queue and updated to the corresponding Rset.

**Yellow**: The dirty card is generated too quickly, and all Refinement threads are activated, as specified by the -XX:G1ConcRefinementYellowZone=N parameter.

**Red**: The dirty card is generated too quickly, and the application thread is also added to the work of draining the queue, slowing down the application thread and dirty card generation.

**Three-Color Marking Algorith**
In this algorith G1's starts from GC root continuously traversing objects and marking with black recheable object. After the entire checking the objects that still in white as the objects to be collected.

During the marking process, the marking threads run alternately with user threads, so changes in references may occur during marking. So in this process that should be market was not market.
### G1 Reviews
From G1's design perspective, it uses a significant number of additional structures to store reference relationships, which helps reduce the time spent on marking during garbage collection. However, these additional structures also lead to memory waste, which, in extreme cases, can consume up to an additional 20% of memory. The region-based memory division scheme makes memory allocation more complex, but it also has its advantages. That is, less fragmentation is generated after memory reclamation, thereby reducing the frequency of Full GCs.

Based on experience, on most large memory servers (6GB and above), G1 outperforms CMS in both throughput and STW time.
## ZGC
Called a “fully concurrent, low latency garbage collector capable of keeping pause time within 10ms”. ZGC stands for Z Garbage Collector. ZGC started as an experimental feature in JDK11 and was fully implemented in JDK17.

ZGC also divides the heap memory into a series of memory partitions called pages.

This management approach is very similar to G1 CG, however, ZGC’s does not support generational features. Therefore, ZGC was initially designed as a non-generational GC mode, with plans to iterate and possibly introduce generational support in the future. There are three types of ZGC pages.
- Small page
- Capacity 2M, store objects less then 256k.
- Medium page
- Capacity 32M, store objects greater than or equal to 256k but less than 4M.
- Large page
- The capacity is not fixed but must be an integer multiple of 2M. Store objects greater than 4M but can store only one object.
### Memory Reclamation Algorith
ZGC’s collection algorith also follows the steps of first finding garbage and then collection it, with the most complex part beign the process of identifying the garbage.