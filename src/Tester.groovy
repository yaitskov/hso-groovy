/** Daneel Yaitskov */
class Tester {

    static isConnected() {
        def pingPro = "ping -c 1 8.8.4.4".execute()

        if (pingPro.waitFor()) {
            println pingPro.text
            return false
        }
        true
    }
}
