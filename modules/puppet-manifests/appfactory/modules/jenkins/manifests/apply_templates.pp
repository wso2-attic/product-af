#Apply templates

define jenkins::apply_templates (
  $jenkins_base_dir,
){
  file {
    "${jenkins_base_dir}/${name}":
      ensure  => present,
      owner   => $owner,
      group   => $group,
      mode    => '0755',
      content => template("jenkins/${name}.erb");
  }
}
