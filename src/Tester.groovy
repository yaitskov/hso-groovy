/** Daneel Yaitskov */
class Tester {

    static isConnected() {
        def pingPro = "ping -c 1 8.8.4.4".execute()

        if (pingPro.waitFor()) {
            print "-\b-"
            return false
        }
        print "+\b+"
        true
    }
}
