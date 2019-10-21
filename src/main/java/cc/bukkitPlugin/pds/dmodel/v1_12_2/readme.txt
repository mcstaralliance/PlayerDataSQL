1.12.2模板使用规则

插件共提供了两个模板用于1.12.2的同步模块编写
以下统称Capability为特性


1. CapabilityProvider型,特性提供器

    需求:
        需要继承自net.minecraftforge.common.capabilities.ICapabilitySerializable,或
        继承net.minecraftforge.common.capabilities.ICapabilityProvider,其中,前者继承自后者

    判断依据:
        一般在Forge事件AttachCapabilitiesEvent中,会有一个 特性 CapabilityProvider的添加过程
        模组会在该事件中通过调用AttachCapabilitiesEvent.addCapability来添加 特性提供器


    ＣapabilityProvider的序列化:
          接口net.minecraftforge.common.capabilities.ICapabilityProvider默认未实现NBT序列化接口INBTSerializable,
          接口net.minecraftforge.common.capabilities.ICapabilitySerializable已经实现接口INBTSerializable
          对于已经实现INBTSerializable的提供器,模板都能自动序列化数据,但是对于未实现的模组,需要自己重写序列化与
          反序列化方法或者替换相应的方法




2. Capability型

    需求:
        通常,Capability型模组都会由CapabilityInject注释的一个静态字段或方法,方法与字段必须为静态
        其中字段的形式的模块已经实现,方法形式的暂时未实现

    注意
        如果,存在CapabilityProvider,则优先使用情况1的方法