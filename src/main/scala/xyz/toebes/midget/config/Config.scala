package xyz.toebes.midget.config

import com.typesafe.config.ConfigFactory

object Config {
  private def config = ConfigFactory.load()

  def settings = config.getString("settings")
  def rules = config.getString("rules")
  def abn = config.getString("abn")
  def aegon = config.getString("aegon")
  def output = config.getString("output")
}
