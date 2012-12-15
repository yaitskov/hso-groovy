/** Daneel Yaitskov */
class Util {
    static passGen(String abc, int len) {
        (1..len).collect {
            abc[new Random().nextInt(abc.length())]
        }.join()
    }
}
