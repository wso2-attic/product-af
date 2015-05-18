#Apply templates

define jenkins::apply_templates (
  $jenkins_home,
){
  file {
    "${jenkins_home}/${name}":
      owner   => $user,
      group   => $group,
      mode    => '0644',
      content => template("jenkins/${name}.erb");
  }
}
