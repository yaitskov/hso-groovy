/** Daneel Yaitskov */
class Tester {

    static isConnected() {
        try {
            def url = "http://www.google.ru/?q=" + Util.passGen("abcdefghijqwervkl", 10)
            println "Test connectivity with $url"
            return url.toURL().text.length() > 0;
        } catch (e) {
            println e
        }
    }
}
