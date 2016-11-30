/*
 * Copyright 2001-2015 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

package object prop {

  def intsBetween(from: Int, to: Int): Generator[Int] =
    new Generator[Int] { thisIntGenerator =>
      private val fromToEdges = List(from, to).distinct // distinct in case from equals to
      override def initEdges(maxLength: Int, rnd: Randomizer): (List[Int], Randomizer) = {
        require(maxLength >= 0, "; the maxLength passed to next must be >= 0")
        val (allEdges, nextRnd) = Randomizer.shuffle(fromToEdges, rnd)
        (allEdges.take(maxLength), nextRnd)
      }
      def next(size: Int, edges: List[Int], rnd: Randomizer): (Int, List[Int], Randomizer) = {
        require(size >= 0, "; the size passed to next must be >= 0")
        edges match {
          case head :: tail => (head, tail, rnd)
          case _ =>
            val (nextInt, nextRandomizer) = rnd.chooseInt(from, to)
            (nextInt, Nil, nextRandomizer)
        }
      }
    }

  def values[T](seq: T*): Generator[T] =
    new Generator[T] {
      def next(size: Int, edges: List[T], rnd: Randomizer): (T, List[T], Randomizer) = {
        require(size >= 0, "; the size passed to next must be >= 0")
        edges match {
          case head :: tail =>
            (head, tail, rnd)
          case _ =>
            val (nextInt, nextRandomizer) = rnd.chooseInt(0, seq.length - 1)
            val nextT = seq(nextInt)
            (nextT, Nil, nextRandomizer)
        }
      }
    }

  val bytes: Generator[Byte] = Generator.byteGenerator
  val shorts: Generator[Short] = Generator.shortGenerator
  val ints: Generator[Int] = Generator.intGenerator
  val longs: Generator[Long] = Generator.longGenerator
  val chars: Generator[Char] = Generator.charGenerator
  val floats: Generator[Float] = Generator.floatGenerator
  val doubles: Generator[Double] = Generator.doubleGenerator
  val strings: Generator[String] = Generator.stringGenerator
  def lists[T](implicit genOfT: Generator[T]): Generator[List[T]] = Generator.listGenerator[T]

  // If I give them a method that offers (A, B) => C. C => A, and C => B functions, then
  // they can get composed shrinkers. And that's the general one:
  //
  // trait Generator2[A, B, C](abc: (A, B) => C, ca: C => A, cb: C => B)(genOfA: Generator[A], genOfB: Generator[B]) extends Generator[C] {
  // }
  def gen[A, B, C](f: (A, B) => C)(implicit genOfA: Generator[A], genOfB: Generator[B]): Generator[C] = {
    val tupGen: Generator[(A, B)] = Generator.tuple2Generator[A, B]
    for (tup <- tupGen) yield f(tup._1, tup._2)
  }

  // gen(
  //   (name: String, age: Int) => Person(name, age), 
  //   (p: Person) => p.name,
  //   (p: Person) => p.age
  // )  // This one takes the implicits at the back. I'd do overloading again as is my wont.

  // gen { (name: String, age: Int) => Person(name, age), }  // This one also takes the implicits at the back. I'd do overloading again as is my wont. It uses canonicals in the shrink/grow method.
 // I wonder if I could just leave this off. They can get this with a for expression.

  // gen(nonEmptyStringValues, posZIntValues)(
  //   (name: String, age: Int) => Person(name, age), 
  //   (p: Person) => p.name,
  //   (p: Person) => p.age
  // )
  // Here I couldn't overload. Hmm.

  /*
    ints
    chars
    longs
    bytes
    shorts
    floats
    doubles
    booleans
    posZInts
    posZIntValues
    nonEmptyStrings
    nonEmptyStringValues
    and so on...

    mabye shrink can be growTo(target: T
    grower(value: T, ...)
    grow(value: T, ...)
  */
}

