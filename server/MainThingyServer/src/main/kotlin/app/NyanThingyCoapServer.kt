package app

import app.resources.LocationResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.elements.util.SslContextUtil
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.config.DtlsConfig
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.CertificateType
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier
import java.net.InetSocketAddress

class NyanThingyCoapServer : CoapServer() {

    // "static" definitions
    companion object
    {
        //reading values from californium3.properties
//        private val SERVER_PORT: Int = Configuration.getStandard().get(CoapConfig.COAP_SECURE_PORT)
//        private const val SERVER_HOSTNAME = "0.0.0.0"
        private const val SERVER_HOSTNAME = "127.0.0.1"
        private const val SERVER_PORT = 5683
        private const val TRUST_STORE_PASSWORD = "rootPass"
        private const val KEY_STORE_PASSWORD = "endPass"
        private const val KEY_STORE_LOCATION = "../../demo-certs/src/main/resources/keyStore.jks"
        private const val TRUST_STORE_LOCATION = "../../demo-certs/src/main/resources/trustStore.jks"
    }

    //configuring the server
    init {

        //Setting the server configuration
        val config = Configuration.getStandard()
        val bindToAddress = InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT)

        // Pre-shared secret
        val pskStore = AdvancedSinglePskStore(
            "identity",
            "secret".toByteArray()
        )

//        // load the key store for certificate
//        val serverCredentials = SslContextUtil.loadCredentials(
//            SslContextUtil.CLASSPATH_SCHEME + KEY_STORE_LOCATION, "server", KEY_STORE_PASSWORD.toCharArray(),
//            KEY_STORE_PASSWORD.toCharArray()
//        )
//        val trustedCertificates = SslContextUtil.loadTrustedCertificates(
//            SslContextUtil.CLASSPATH_SCHEME + TRUST_STORE_LOCATION, "root", TRUST_STORE_PASSWORD.toCharArray()
//        )
//
//        // The x509 certificate
//        val certificateIdentityProvider = SingleCertificateProvider(
//            serverCredentials.privateKey, serverCredentials.certificateChain, CertificateType.X_509
//        )
//
//        val certificateVerifier = StaticNewAdvancedCertificateVerifier.builder()
//            .setTrustedCertificates(*trustedCertificates)
//            .setTrustAllRPKs()
//            .build()

        val dtlsConfig = DtlsConnectorConfig.builder(config)
            .setAddress(bindToAddress)
            .setAsList(DtlsConfig.DTLS_CIPHER_SUITES,
                //the cipher suite for psk
                CipherSuite.TLS_PSK_WITH_AES_128_CCM_8,
                //the cipher suite for x509
//                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8
            )
            .setAdvancedPskStore(pskStore)
//            .setCertificateIdentityProvider(certificateIdentityProvider)
//            .setAdvancedCertificateVerifier(certificateVerifier)
        .build()

        addEndpoint(
            CoapEndpoint.Builder()
            .setConfiguration(config)
            .setConnector(
                DTLSConnector(dtlsConfig)
            )
            .build())


        //Adding the resources
        add(
            LocationResource()
        )
    }

}