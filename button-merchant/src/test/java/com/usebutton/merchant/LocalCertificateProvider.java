/*
 * LocalCertificateProvider.java
 *
 * Copyright (c) 2019 Button, Inc. (https://usebutton.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.usebutton.merchant;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

final class LocalCertificateProvider extends CertificateProvider {

    String[] hashedKeys = {
            "testhashedkey",
            "justfortesting"
    };
    Set<String> publicKeyset = new HashSet<>(Arrays.asList(hashedKeys));

    String[] certs = {
            "-----BEGIN CERTIFICATE-----\n"
                    + "MIIFiDCCA3ACCQDihOZmodlMXjANBgkqhkiG9w0BAQsFADCBhTELMAkGA1UEBhMC\n"
                    + "VVMxCzAJBgNVBAgMAk5ZMQwwCgYDVQQHDANOWUMxDzANBgNVBAoMBkJ1dHRvbjET\n"
                    + "MBEGA1UECwwKRXhwZXJpZW5jZTESMBAGA1UEAwwJbG9jYWxob3N0MSEwHwYJKoZI\n"
                    + "hvcNAQkBFhJuYWptQHVzZWJ1dHRvbi5jb20wHhcNMTkwNjExMjA1NjI1WhcNMjkw\n"
                    + "NjA4MjA1NjI1WjCBhTELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAk5ZMQwwCgYDVQQH\n"
                    + "DANOWUMxDzANBgNVBAoMBkJ1dHRvbjETMBEGA1UECwwKRXhwZXJpZW5jZTESMBAG\n"
                    + "A1UEAwwJbG9jYWxob3N0MSEwHwYJKoZIhvcNAQkBFhJuYWptQHVzZWJ1dHRvbi5j\n"
                    + "b20wggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQC3eS6jZBR6+yMMjyhd\n"
                    + "j+b/x0DfogfL4WpRKs8MDvQmArBUn/g4RPGwsDk+OBeGA7VjOmO7M/3/RuOU09LP\n"
                    + "Rwv5BkEY3l3M+Plcq58F5xbKWEsY7s6MoYJRfcti/vcQv8vrCp+XAoqe975DNeBN\n"
                    + "0uUXatCMMWBFTO50O+icWr8nQj5B7FTqsXsXKsW2miiSQHdGSGAiUBxChpTHeufU\n"
                    + "SG614MG7cgY3lY9RA99rMX4BUGMr5ShjjrQnYSEFITo9hX9763EP3gJiIG4T56xf\n"
                    + "zBF8rMvF1kYxA3hiAvRn5/az5GZn5Ywz8wyrtnjzmaayZGl2LqCfgeAk3fa5dvws\n"
                    + "WzGD+dV3KGyKxLhEKnDPBSPoee+hZ+D6GJtXIt4Z7c0hdZlLUmeZfPCtNMwB5MS8\n"
                    + "EawPA9UZaNB8bbZBro6cMBTd5cDl4oJ0EWTY3fIzNo5Mv+fMH5pOIRi73m78llBk\n"
                    + "QtRM8PtzmG7rxHdc5UaNF7uFxFwv66zF7yNGvMZJH/Rdt4oWv6d+bhnnOuce/8LL\n"
                    + "+/rgvhOPNw3BElNH1QbPDRURZm5d5b3BkuUJTD9/jxddjDik9ug+lIvgOyVGaqmQ\n"
                    + "fIQYz01hl9ALMvx0wQRfijLRUyh+rbf3t05PAvOk66THHvWRnCkaqyY0icqZbhQb\n"
                    + "v2/aKDmxy6rVbT+Gg8RysUCTVQIDAQABMA0GCSqGSIb3DQEBCwUAA4ICAQA3AnpM\n"
                    + "tY4J4XyFnWb0HXQkKY2imYe526+6BAGQfTdQrlqTmn+VROn6e7/4O0kDkenx/a+b\n"
                    + "NEPcpCj23p3d8IbyLu6560ZoxVSxYvP3+Ek+ZWb77x7DZLLj/ZyxA2lP1QbRLfT/\n"
                    + "JzwzZjaHF04VCm6w+5HFWPmNOhksYmjIoUPvK/Vg8tFEFPco3DBl2BtOqL/2H41z\n"
                    + "eybncCHP8kPiPT05uGnqf6nIvD9+cfYbvi/BzafV0aXxSAfq3cem0hRZau9nV6gB\n"
                    + "ehEVsGeqPYCTUws3Exv5mKTmAuk09kWeEoVRePNkTRKY60/f+/RX7bJMfCzogWFi\n"
                    + "6z/UD47+E5QAYF6hl4FgxAlDhlRE9pLGPzIoXnnJHsgY2ePh3FFzBFr2ZGIoOHa2\n"
                    + "YPLOBdeIwfHt1cpcmr/MmfUHw/6x1m/Cv8ObrXbBjPrCJtPNgrUjwDXGqDwGarVu\n"
                    + "WuN92QAFMMm7jWwncf/4qYVjFTaDyCVQNWV50rtYeEaIVRMtpolB7CvYVb3CdH5m\n"
                    + "cWxA8T6i1q/8gUUQgRCcH/XtZ7jCQEy7trGQhg5LtyrEYM/phkerTMDYM3bT4exs\n"
                    + "gIKS49qM9n+gXuuweWqFxQ95aQ1RqVOeHpyBnGwOLSVbLywFQAu3d2SKGBlmIafN\n"
                    + "MGHKu2M7Q0sz5mvP1FgSnCu8XcyOR6Te0uF56w==\n"
                    + "-----END CERTIFICATE-----\n",

            "-----BEGIN CERTIFICATE-----\n"
                    + "MIIFgDCCA2gCAQEwDQYJKoZIhvcNAQEFBQAwgYUxCzAJBgNVBAYTAlVTMQswCQYD\n"
                    + "VQQIDAJOWTEMMAoGA1UEBwwDTllDMQ8wDQYDVQQKDAZCdXR0b24xEzARBgNVBAsM\n"
                    + "CkV4cGVyaWVuY2UxEjAQBgNVBAMMCWxvY2FsaG9zdDEhMB8GCSqGSIb3DQEJARYS\n"
                    + "bmFqbUB1c2VidXR0b24uY29tMB4XDTE5MDYxMTIxMDEyM1oXDTI5MDYwODIxMDEy\n"
                    + "M1owgYUxCzAJBgNVBAYTAlVTMQswCQYDVQQIDAJOWTEMMAoGA1UEBwwDTllDMQ8w\n"
                    + "DQYDVQQKDAZCdXR0b24xEzARBgNVBAsMCkV4cGVyaWVuY2UxEjAQBgNVBAMMCWxv\n"
                    + "Y2FsaG9zdDEhMB8GCSqGSIb3DQEJARYSbmFqbUB1c2VidXR0b24uY29tMIICIjAN\n"
                    + "BgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsQ439NBYQROb9c0wh3ikiCu0uBg2\n"
                    + "/s+5GbKwnjDyrdpAL6lp/5Zi1W941Tz8oUG35YCmNCWDIb493OdYX55v1RMMZlJd\n"
                    + "tki+cUKmKoKFzbYJd/MhrtWB7zlO6Z3vmal2qjiBN1jEBqrnJmAUf0zJnyeyWJdg\n"
                    + "B3kd9GqNajHSep4Gu++79ZTUO7AecFYUFlNBJ67qf7rpjH7hYB71F0hldEAAGWtc\n"
                    + "RDPjLFtBreuvwtNVbYgu41tiNR4bxF23Ud0NRazzOkyqJ1KyIw9gA8e6poSRgFPm\n"
                    + "vWd+C1rMECrNrO+3fF3D8HLG5jnzATvdEAGO0e1yUyU8qJE79Ob3wd0SC1d60mAG\n"
                    + "Raat+maEB46vXvxfRR2YAgtWC+8SAhz5WPiDjEPpkU+nwYDOYsgu3kjTKp17eIw5\n"
                    + "oUpMVbBdDIU9eyktIF5FFcG3EpPAm6nFpEPmj0v1V3AluB7tvGqDzFyZrWTzRwRb\n"
                    + "1EOg8hQp5oj+mzkx/dI06jvKMoNY/3iZrmXQ7plB6lhto81PGscSIKtcZrhIEJD0\n"
                    + "G6tyL9eK8BKlyxD6FmqQK+uQ8S5DWi7+VNhwtUqmTRjkRSZYbnpuBweQzWQKRqjB\n"
                    + "bT7Dxvan2P/jCCmvb7jHI1Nz/mBTz9rnP0+IfHKRZsbavKvtMZTeQaggiVDt24Zu\n"
                    + "1h6aCTyuLflCo2ECAwEAATANBgkqhkiG9w0BAQUFAAOCAgEADWugvmWwpO17wdR8\n"
                    + "sgvLbyUVONO4zQi6+BiAKLj6gUVscW/obxS6l6lazBLEg9RRgOFNnbYRHoHIR+yJ\n"
                    + "VFcr13txkiJREIHz2P5lri5DOtpGIdkiUF5FFq6haziLtmlJAier2ggXx7XR241O\n"
                    + "EozC5YDsv50fv6WpKdcDygbMaEptUFNcHtRdmZuI2Xk2f9tDAkS/mVmyCT+wg6KM\n"
                    + "ATA6J35L3MFtDIBWESznhl3jlXwIz/3LBf1tUY0DxzmOR4dylO8QuSt1OJFjNpDz\n"
                    + "OvKonXW0Lr3mk2qcpHKQTHMpoMfwMdCjCaI7Vz789PqiHW0f5ZGVDiP3+tSvePQI\n"
                    + "Iv2t9J5wto8E7GAHnmVDAbTFvuNbGjPRMKvgdeBtYmpBiSSXbelCCNkotrzLZuf2\n"
                    + "03TNee6V65qU7hEvUbZA5+9/3Z3BQadQ2GITtJQZ0kPwYO2t3DLiFQxQizSrKJz+\n"
                    + "168pLtxUl6PnRy32CF8kBxrSyMlbbgpa/yGxG5gGwsLcuOYximV5fqvczCS2M9GJ\n"
                    + "/uH3w0zDbkc5pMRC2kAAvtnrdlEZV+8K6S4OwJ7D+MlVUi0ayOuUSPRul+qdAPAf\n"
                    + "w3ujqleRtUu2xLuAwInQrIs1PoFLXzr4FoiVcxGUCZ2M71SMBpZWyC0B2OQ5FQ7k\n"
                    + "Z7zwyvJ1duJGACv3poVh9yedlEY=\n"
                    + "-----END CERTIFICATE-----\n"
    };

    @Override
    String[] getCertificates() {
        return certs;
    }

    @Override
    public Set<String> getPublicKeys() {
        return publicKeyset;
    }

    @Override
    String getProviderName() {
        return "localhost";
    }
}
