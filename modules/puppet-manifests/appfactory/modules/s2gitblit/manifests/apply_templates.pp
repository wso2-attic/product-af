#Apply templates

define s2gitblit::apply_templates (
  $gitblit_home,
){
  file {
    "${gitblit_home}/${name}":
      owner   => $user,
      group   => $group,
      mode    => '0644',
      content => template("s2gitblit/${name}.erb");
  }
}
