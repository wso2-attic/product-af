#Apply templates

define jenkins::apply_templates (
  $jenkins_base_dir,
){
  file {
    "${jenkins_base_dir}/${name}":
      owner   => $user,
      group   => $group,
      mode    => '0644',
      content => template("jenkins/${name}.erb");
  }
}
