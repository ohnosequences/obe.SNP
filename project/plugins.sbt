
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

resolvers += "Era7 maven snapshots" at "http://snapshots.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.4.0")

addSbtPlugin("org.scala-sbt.plugins" % "sbt-onejar" % "0.8")

