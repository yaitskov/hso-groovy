/**
 *
 * Daneel Yaitskov
 * */
class Modem {

    def apn = "internet.beeline.ru"
    def device = '/dev/ttyHS0'
    def netdev = 'hso0'
    def endl = "\n"
    /**
     * Ip address of this host
     */
    def ipAddr
    /**
     * List of name server ips
     */
    def nameServers

    /**
     * init modem and makes connection (gets IP and name servers)
     * @throws IOException
     */
    def up() {
        println "connecting..."
        init()
        getIpDnsNss()
        if (ipAddr)
            setGotConfig()
        println "done."
    }

    /**
     *
     * @param cmds file with commands for chat program
     * @return lines of output file
     * @throws IOException
     */
    private sendCommand(File cmds) {
        // string method execute doesn't fit because it's not genuine bash
        def chat = ['bash', '-c',
                "/usr/sbin/chat -E -s -V -f ${cmds.absolutePath} < $device > $device"]
        println "command: ${chat.join(' ')}"
        def chatPro = chat.execute()
        chatPro.waitFor()
        // exit code can be not zero and it's ok
        chatPro.err.text.split "\n"
    }

    /**
     * boot strap modem. nothing to return.
     * @throws IOException
     */
    private init() {
        def cmds = TmpFile.create()
        try {
            cmds << 'ABORT ERROR' << endl << 'TIMEOUT 10' << endl <<
                    '"" ATZ' << endl <<
                    // skip pin
                    'OK "AT+COPS=0^m"' << endl <<
                    'OK "\\d\\d\\d\\d\\d\\d\\dAT+COPS=?^m"' << endl <<
                    'OK "AT+CGDCONT=1,\\"IP\\",\\"' << apn <<
                    '\\",\\"0.0.0.0\\",0,0^m"' << endl <<
                    'OK ""' << endl
            def output = sendCommand(cmds)
            def errors = output.grep { it =~ /^(ERROR|[+]CME)/ }
            if (errors) {
                throw new IOException("cannot getIpDnsNss: ${errors.join(', ')}")
            }
        } finally {
            cmds.delete()
        }
    }

    private getIpDnsNss() {
        def cmds = TmpFile.create()
        try {
            cmds << 'ABORT ERROR' << endl << 'TIMEOUT 10' << endl <<
                    '"" ATZ' << endl << 'OK "AT_OWANCALL=1,1,0^m"' << endl <<
                    'OK "\\d\\d\\d\\d\\dAT_OWANDATA=1^m"' << endl <<
                    'OK ""' << endl
            def attempts = 0
            while (!ipAddr && attempts < 10) {
                attempts++
                println "attempt $attempts"
                def output = sendCommand(cmds)
                def owan = output.find { it =~ /^_OWANDATA/ }
                if (owan) {
                    owan = owan.split(',')*.trim()
                    if (owan.size() < 5) {
                        println "owan to shot: ${owan.join(', ')}"
                        continue
                    }
                    (ipAddr, nameServers) = [owan[1], owan[3..4]]
                    println "Connected. I've got IP = $ipAddr and DNSs ${nameServers.join(' ')}"
                }
            }
        } finally {
            cmds.delete()
        }
    }


    private setGotConfig() {
        ifconfig()
        addRoute()
        setupNameServers()
    }

    def netMask = '255.255.255.255'

    private void ifconfig() {
        println "setting IP address $ipAddr"
        // get current address assigned to netdev
        def ifconfigOutput = Cmd.few("ifconfig").split("\n");
        def hsoLine = ifconfigOutput.findIndexOf { it =~ /$netdev/ }
        if (hsoLine < 0) {
            println "    net device '$netdev' is down"
            Cmd.few "ifconfig $netdev $ipAddr netmask $netMask up"
        } else {
            println "   checking ip address "
            def hsoIpMask = ifconfigOutput[hsoLine + 1]
            def matched = hsoIpMask =~ /inet addr: *([0-9.]+).*Mask: *([0-9.]+).*/;
            if (matched) {
                if (matched[0][1] != ipAddr || matched[0][2] != netMask) {
                    println "      update ip address to $ipAddr"
                    Cmd.few "ifconfig $netdev $ipAddr netmask $netMask up"
                }
            }
        }
    }

    private void addRoute() {
        println "checking routes"
        def routes = Cmd.few("route").split "\n"
        def netDevRoutes = routes.grep { route -> route =~ /$netdev *$/ }
        def validRoutes = netDevRoutes.grep { route ->
            def match = route =~ /^([^ \t]+).*/
            if (match[0][1] != 'default') {
                println "   delete route ${match[0][1]}"
                Cmd.few "route del ${match[0][1]}"
                return false
            }
            return true
        }
        if (!validRoutes) {
            println "   adding new route"
            Cmd.few "route add default dev $netdev"
        }
    }

    private void setupNameServers() {
        def resolv = new File("/etc/resolv.conf")
        resolv.delete()
        resolv << nameServers.collect { "nameserver $it" }.join("\n")
    }

    def down = {

    }
}
