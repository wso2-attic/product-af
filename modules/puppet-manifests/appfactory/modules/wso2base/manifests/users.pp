# The user’s password, in encrypted format(sha-512) which is used by the latests
# ubuntu releases (10.04 LTS ,12.04LTS, 14.04 LTS, 14.10,15.04 ).
# Here we are using "afpuppet" as the default password.
# To generate a password hash to use with puppet manifest files you can use the mkpasswd utility.
# (In ubuntu execute following command to generate encrpted password)
# mkpasswd -m sha-512
define wso2base::users($puppet_username = 'afpuppet', $puppet_password = '$6$o5FkMRhRsLBpLRy$.W5wEhq8xqWdVs2ULLXEL2WPbWMB9voAfu5TBeCJ0Jo2voQG.j8/Jf5M9oh4UhV05TL3uAoXlU4uyy1bRcSRo.'){

    user { $puppet_username:
        password   => $puppet_password,
        ensure     => present,                            
        managehome => true,
        shell      => '/bin/bash',
    }
}
