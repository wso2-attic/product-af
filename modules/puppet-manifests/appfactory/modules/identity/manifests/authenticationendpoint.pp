define identity::authenticationendpoint ($security, $target, $owner, $group ) {
  #This is to deploy the authenticationendpoint.war.
  #The need for this is that we have to dynamically generate the redirection URL

  $file_name        = "authenticationendpoint.war"
  $extracted_name   = "authenticationendpoint"
  $file_path        = "repository/deployment/server/webapps"
  $template_name    = "deployment/server/webapps/authenticationendpoint/appfactory/login_ajaxprocessor.jsp"

  #Extracting the war file
  exec{
    "extracting ${file_name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    cwd       => "/tmp/${identity::deployment_code}/${file_path}",
    unless    => "test -d /tmp/${identity::deployment_code}/${file_path}/${extracted_name}/appfactory",
    command   => "unzip /tmp/${identity::deployment_code}/${file_path}/${file_name} -d /tmp/${identity::deployment_code}/${file_path}/${extracted_name}",
    logoutput => 'on_failure',
    timeout   => 0
  }

  #Removing the war file
  exec{
    "removing ${file_name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    cwd       => "/tmp/${identity::deployment_code}/${file_path}/",
    command   => "rm ${file_name}",
    logoutput => 'on_failure',
    timeout   => 0,
    require   => Exec["extracting ${file_name}"];
  }

  #Applying the template
  file { "/tmp/${identity::deployment_code}/${file_path}/${extracted_name}/appfactory/login_ajaxprocessor.jsp":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("${identity::deployment_code}/${template_name}.erb"),
    require => Exec["extracting ${file_name}"]
  }

  #Repacking the war file
  exec{
    "packing ${file_name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    cwd       => "/tmp/${identity::deployment_code}/${file_path}/${extracted_name}",
    command   => "zip -r /tmp/${identity::deployment_code}/${file_path}/${file_name} *",
    logoutput => 'on_failure',
    timeout   => 0,
    require   => [File["/tmp/${identity::deployment_code}/${file_path}/${extracted_name}/appfactory/login_ajaxprocessor.jsp"],
                 Exec["removing ${file_name}"]]
  }
}
