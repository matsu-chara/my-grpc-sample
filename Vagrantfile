# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "ubuntu/trusty64"
  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
    # Display the VirtualBox GUI when booting the machine
    # vb.gui = true

    # Customize the amount of memory on the VM:
    vb.memory = "1024"
  end
  #
  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  config.vm.provision "shell", privileged: false, inline: <<-SHELL
    # scala
    ## install sbt
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
    sudo apt-get update
    sudo apt-get -y install sbt

    ## install project dependency
    cd /vagrant/grpc-scala
    sbt update
    cd ~

    # php
    ## install protoc
    sudo apt-get -y install git autoconf libtool unzip g++
    git clone https://github.com/google/protobuf
    cd protobuf
    ./autogen.sh
    ./configure
    make
    make check
    sudo make install
    sudo ldconfig
    cd ~

    ## install php
    sudo apt-get -y install php5 php5-dev php-pear
    sudo apt-get -y install nginx php5-fpm
    curl -sS https://getcomposer.org/installer | php
    sudo mv composer.phar /usr/local/bin/composer

    # install Protobuf-PHP
    sudo pear channel-discover pear.pollinimini.net
    sudo pear install drslump/Protobuf-beta

    # ## install grpc-php
    echo "deb http://http.debian.net/debian jessie-backports main" | sudo tee -a /etc/apt/sources.list
    sudo apt-get update
    sudo apt-get -y --force-yes install libgrpc-dev
    sudo pecl install grpc-beta

    ## configure nginx, php5-fpm
    echo "extension=grpc.so" | sudo tee -a /etc/php5/fpm/php.ini
    sudo sed -i '37a\\\tlocation ~ \.php$ {\\n\\t\\tinclude snippets/fastcgi-php.conf;\\n\\t\\tfastcgi_pass unix:/var/run/php5-fpm.sock;\\n\\t}' /etc/nginx/sites-available/default

    ## restart services
    sudo service nginx restart
    sudo service php5-fpm restart

    ## init project
    cd /vagrant/grpc-php
    composer update
  SHELL
end
